package at.salzburg.hackathon.hackathontourism03.service.logics;

import at.salzburg.hackathon.hackathontourism03.domain.PlaceEntity;
import at.salzburg.hackathon.hackathontourism03.domain.ReportEntity;
import at.salzburg.hackathon.hackathontourism03.domain.ReviewEntity;
import at.salzburg.hackathon.hackathontourism03.domain.ReviewId;
import at.salzburg.hackathon.hackathontourism03.domain.UserEntity;
import at.salzburg.hackathon.hackathontourism03.persistence.PlaceRepository;
import at.salzburg.hackathon.hackathontourism03.persistence.ReportRepository;
import at.salzburg.hackathon.hackathontourism03.persistence.ReviewRepository;
import at.salzburg.hackathon.hackathontourism03.persistence.UserRepository;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.ReportInputTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.ReviewInputTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.VoteTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.*;
import at.salzburg.hackathon.hackathontourism03.service.Features;
import at.salzburg.hackathon.hackathontourism03.service.Features.Feature;
import at.salzburg.hackathon.hackathontourism03.service.PlaceDataset;
import at.salzburg.hackathon.hackathontourism03.service.PlaceDataset.Attribute;
import at.salzburg.hackathon.hackathontourism03.service.PlaceDataset.BasePlace;
import at.salzburg.hackathon.hackathontourism03.service.PlaceDataset.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LocationLogic {
    public static final String DEMO_USERNAME = "Demo Visitor";

    private static final int DEFAULT_LIST_SIZE = 210;
    private static final int SEARCH_LIMIT = 50;
    private static final int REVIEW_POINTS = 10;
    private static final int REVIEW_EDIT_POINTS = 5;
    private static final int REPORT_POINTS = 5;
    private static final int CONFIRMATION_POINTS = 10;
    private static final Map<String, String> SOURCE_LABELS = Map.of(
            "osm", "OpenStreetMap",
            "tourdata", "Upper Austria Tourism",
            "gtfs_ooevv", "Timetable",
            "doris", "Land Oberösterreich",
            "crawl", "Business website",
            "user", "Community",
            "host", "Business owner");
    private static final Map<String, String> REPORT_ICONS = Map.of(
            "elevator_broken", "ArrowUpDown",
            "path_blocked", "Accessibility",
            "ramp_unusable", "Accessibility",
            "restroom_unavailable", "PersonStanding",
            "loud_noise", "Volume2");

    @Autowired
    private PlaceDataset dataset;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReportRepository reportRepository;

    public List<LocationTO> getLocations(String query) {
        requireData();
        String needle = PlaceDataset.normalize(query.trim());
        return toLocations(needle.isEmpty() ? defaultPlaces() : search(needle));
    }

    public LocationTO getLocation(String id) {
        return toLocations(List.of(requirePlace(id))).get(0);
    }

    public CatalogTO getCatalog() {
        requireData();
        return new CatalogTO(
                Features.AMENITIES.stream().map(a -> new FeatureTO(a.id(), a.label(), a.icon(), Features.conflictsOf(a))).toList(),
                Features.OBSTACLES.stream().map(o -> new FeatureTO(o.id(), o.label(), o.icon(), List.of())).toList(),
                dataset.reportTypes().stream()
                        .map(r -> new ReportTypeTO(r.id(), r.label(), REPORT_ICONS.getOrDefault(r.id(), "TriangleAlert")))
                        .toList(),
                dataset.attribution());
    }

    public ProfileTO getProfile() {
        return toProfile(currentUser());
    }

    public List<AchievementTO> getAchievements() {
        ProfileTO profile = getProfile();
        return List.of(
                achievement("welcome", "A more accessible world", "Join a community that opens doors for everyone.", "HeartHandshake", 1, 1),
                achievement("first-review", "First steps", "Share your first accessibility review.", "Footprints", 1, profile.reviewCount()),
                achievement("reporter", "Looking out for others", "Report your first live accessibility issue.", "ShieldCheck", 1, profile.reportCount()),
                achievement("explorer", "Neighborhood explorer", "Leave 5 reviews and help people find their way.", "Compass", 5, profile.reviewCount()),
                achievement("champion", "Access champion", "Earn 250 community points.", "Trophy", 250, profile.points()),
                achievement("trailblazer", "The pathfinder", "Make a lasting difference with 20 reviews.", "Mountain", 20, profile.reviewCount()));
    }

    @Transactional
    public ReviewResultTO addReview(String placeId, ReviewInputTO input) {
        BasePlace base = requirePlace(placeId);
        Integer rating = input.getRating();
        String text = input.getText() == null ? "" : input.getText();
        if (rating == null || rating < 1 || rating > 5) {
            throw new ApiException(422, "Choose a rating from 1 to 5 stars.");
        }
        if (text.length() > 1000) {
            throw new ApiException(422, "Keep your review under 1,000 characters.");
        }
        Set<String> amenities = validFeatures(input.getAmenities(), Features.AMENITIES);
        Set<String> obstacles = validFeatures(input.getObstacles(), Features.OBSTACLES);
        List<String> attributes = Stream.concat(amenities.stream(), obstacles.stream())
                .map(id -> Features.byId(id).orElseThrow().attribute())
                .toList();
        if (new HashSet<>(attributes).size() != attributes.size()) {
            throw new ApiException(422, "Please check the conflicting amenities and obstacles.");
        }

        UserEntity user = currentUser();
        PlaceEntity place = placeRepository.findById(placeId).orElseGet(() -> {
            PlaceEntity created = new PlaceEntity();
            created.setLocationName(placeId);
            return placeRepository.save(created);
        });
        // A user has one review per place (ReviewId), so submitting again edits it.
        ReviewId id = new ReviewId(placeId, user.getId());
        ReviewEntity review = reviewRepository.findReviewById(id);
        boolean created = review == null;
        if (created) {
            review = new ReviewEntity();
            review.setId(id);
            review.setPublishDate(LocalDateTime.now());
            place.addReview(review);
            user.addReview(review);
        } else {
            review.setLastEditedDate(LocalDateTime.now());
            review.getPositiveRatings().clear();
            review.getNegativeRatings().clear();
        }
        review.setOverallRating(rating);
        review.setMessage(text.trim());
        ReviewEntity saved = review;
        amenities.forEach(amenity -> saved.getPositiveRatings().put(amenity, true));
        obstacles.forEach(obstacle -> saved.getNegativeRatings().put(obstacle, true));

        int points = created ? REVIEW_POINTS : REVIEW_EDIT_POINTS;
        user.setPoints(user.getPoints() + points);
        reviewRepository.save(saved);
        placeRepository.save(place);
        userRepository.save(user);
        return new ReviewResultTO(toLocations(List.of(base)).get(0), toProfile(user), points);
    }

    @Transactional
    public LocationTO addReport(String placeId, ReportInputTO input) {
        BasePlace base = requirePlace(placeId);
        String type = input.getType();
        String description = input.getDescription() == null ? "" : input.getDescription();
        if (expiryDays(type) == null) {
            throw new ApiException(422, "Choose an issue type.");
        }
        if (description.length() > 500) {
            throw new ApiException(422, "Keep details under 500 characters.");
        }
        if (activeReports(placeId).stream().anyMatch(r -> r.getType().equals(type))) {
            throw new ApiException(409, "This issue is already reported. You can confirm it instead.");
        }

        UserEntity user = currentUser();
        LocalDateTime now = LocalDateTime.now();
        ReportEntity report = new ReportEntity();
        report.setId(UUID.randomUUID().toString());
        report.setPlaceId(placeId);
        report.setType(type);
        report.setDescription(description.trim());
        report.setCreatedAt(now);
        report.setLastConfirmedAt(now);
        report.setConfirmations(1);
        report.setReporterId(user.getId());
        report.getConfirmedBy().add(user.getId());
        reportRepository.save(report);
        user.setPoints(user.getPoints() + REPORT_POINTS);
        userRepository.save(user);
        return toLocations(List.of(base)).get(0);
    }

    @Transactional
    public LocationTO voteReport(String placeId, String reportId, VoteTO vote) {
        BasePlace base = requirePlace(placeId);
        if (vote.getStillRelevant() == null) {
            throw new ApiException(422, "Choose whether the issue is still present.");
        }
        ReportEntity report = activeReports(placeId).stream()
                .filter(r -> r.getId().equals(reportId))
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "This issue has already been resolved."));
        if (!vote.getStillRelevant()) {
            report.setResolvedAt(LocalDateTime.now());
        } else {
            UserEntity user = currentUser();
            if (report.getConfirmedBy().add(user.getId())) {
                report.setConfirmations(report.getConfirmations() + 1);
                report.setLastConfirmedAt(LocalDateTime.now());
                user.setPoints(user.getPoints() + CONFIRMATION_POINTS);
                userRepository.save(user);
            }
        }
        reportRepository.save(report);
        return toLocations(List.of(base)).get(0);
    }

    private void requireData() {
        if (!dataset.isLoaded()) {
            throw new ApiException(503, "Place data is not available right now.");
        }
    }

    private BasePlace requirePlace(String id) {
        requireData();
        BasePlace place = dataset.places().get(id);
        if (place == null) {
            throw new ApiException(404, "This place could not be found.");
        }
        return place;
    }

    private UserEntity currentUser() {
        // There is no login in the frontend yet, so everything acts as the demo user.
        return userRepository.findFirstByUsername(DEMO_USERNAME);
    }

    private List<BasePlace> ranked() {
        return dataset.places().values().stream()
                .sorted(Comparator.comparing((BasePlace p) -> p.statements().isEmpty()).thenComparingDouble(BasePlace::metres))
                .toList();
    }

    private List<BasePlace> search(String needle) {
        return ranked().stream().filter(p -> p.searchText().contains(needle)).limit(SEARCH_LIMIT).toList();
    }

    /** The map draws one marker per place, so the default list is the nearest documented places per category. */
    private List<BasePlace> defaultPlaces() {
        Map<String, List<BasePlace>> byCategory = ranked().stream()
                .filter(p -> !p.statements().isEmpty())
                .collect(Collectors.groupingBy(BasePlace::category, LinkedHashMap::new, Collectors.toList()));
        int perCategory = (int) Math.ceil((double) DEFAULT_LIST_SIZE / byCategory.size());
        Set<BasePlace> result = new LinkedHashSet<>();
        byCategory.values().forEach(list -> result.addAll(list.subList(0, Math.min(perCategory, list.size()))));
        Stream.concat(reviewRepository.findReviewedPlaceIds().stream(), reportRepository.findReportedPlaceIds().stream())
                .map(dataset.places()::get)
                .filter(Objects::nonNull)
                .forEach(result::add);
        return new ArrayList<>(result);
    }

    private Set<String> validFeatures(List<String> ids, List<Feature> allowed) {
        Set<String> result = new LinkedHashSet<>(ids == null ? List.of() : ids);
        if (!result.stream().allMatch(id -> allowed.stream().anyMatch(f -> f.id().equals(id)))) {
            throw new ApiException(422, "Choose features from the available list.");
        }
        return result;
    }

    private Integer expiryDays(String type) {
        return dataset.reportTypes().stream()
                .filter(r -> r.id().equals(type))
                .map(PlaceDataset.ReportType::expiresAfterDays)
                .findFirst()
                .orElse(null);
    }

    private boolean isActive(ReportEntity report) {
        Integer days = expiryDays(report.getType());
        return report.getLastConfirmedAt().plusDays(days == null ? 3 : days).isAfter(LocalDateTime.now());
    }

    private List<ReportEntity> activeReports(String placeId) {
        return reportRepository.findByPlaceIdInAndResolvedAtIsNull(List.of(placeId)).stream().filter(this::isActive).toList();
    }

    private List<LocationTO> toLocations(List<BasePlace> places) {
        List<String> ids = places.stream().map(BasePlace::id).toList();
        Map<String, List<LocationReviewTO>> reviews = reviewRepository.findByIdPlaceIdIn(ids).stream()
                .sorted(Comparator.comparing(ReviewEntity::getPublishDate).reversed())
                .collect(Collectors.groupingBy(r -> r.getId().getPlaceId(), Collectors.mapping(this::toReview, Collectors.toList())));
        Map<String, List<LocationReportTO>> reports = reportRepository.findByPlaceIdInAndResolvedAtIsNull(ids).stream()
                .filter(this::isActive)
                .sorted(Comparator.comparing(ReportEntity::getCreatedAt).reversed())
                .collect(Collectors.groupingBy(ReportEntity::getPlaceId, Collectors.mapping(this::toReport, Collectors.toList())));
        return places.stream()
                .map(p -> toLocation(p, reviews.getOrDefault(p.id(), List.of()), reports.getOrDefault(p.id(), List.of())))
                .toList();
    }

    private LocationTO toLocation(BasePlace place, List<LocationReviewTO> reviews, List<LocationReportTO> reports) {
        List<Statement> statements = new ArrayList<>(place.statements());
        reviews.forEach(review -> statements.addAll(statementsOf(review)));
        List<FactTO> facts = resolveFacts(statements);
        double total = reviews.stream().mapToInt(LocationReviewTO::rating).sum();
        double score = reviews.isEmpty() ? 0 : Math.round(total / reviews.size() * 10) / 10.0;
        Map<String, FactTO> byAttribute = facts.stream().collect(Collectors.toMap(FactTO::attribute, f -> f));

        Set<String> amenities = new LinkedHashSet<>();
        Set<String> obstacles = new LinkedHashSet<>();
        for (FactTO fact : facts) {
            Features.byFact(fact.attribute(), String.valueOf(fact.value()))
                    .ifPresent(chip -> addChip(chip, amenities, obstacles));
        }
        // Counts have no direct chip; an explicit step-free fact outranks a step count.
        FactTO steps = byAttribute.get("step_count");
        if (steps != null && !byAttribute.containsKey("step_free_entrance")) {
            addChip(Features.byId(asNumber(steps) > 0 ? "steps" : "ramp").orElseThrow(), amenities, obstacles);
        }
        FactTO parking = byAttribute.get("disabled_parking");
        if (parking != null && asNumber(parking) > 0) {
            addChip(Features.byId("parking").orElseThrow(), amenities, obstacles);
        }

        FactTO wheelchair = byAttribute.get("wheelchair_access");
        return new LocationTO(place.id(), place.name(), place.kind(), place.category(), place.address(), place.lat(), place.lng(),
                place.distance(), "", place.color(), place.website(), place.openingHours(), score, reviews.size(),
                wheelchair == null ? "unknown" : String.valueOf(wheelchair.value()),
                List.copyOf(amenities), List.copyOf(obstacles), facts, reviews, reports);
    }

    private static void addChip(Feature chip, Set<String> amenities, Set<String> obstacles) {
        (Features.isAmenity(chip) ? amenities : obstacles).add(chip.id());
    }

    private static double asNumber(FactTO fact) {
        return fact.value() instanceof Number number ? number.doubleValue() : 0;
    }

    private static List<Statement> statementsOf(LocationReviewTO review) {
        return Stream.concat(review.amenities().stream(), review.obstacles().stream())
                .map(id -> Features.byId(id).orElse(null))
                .filter(Objects::nonNull)
                .map(f -> new Statement(f.attribute(), f.value(), "user", "review:" + review.id(), review.createdAt(), review.createdAt()))
                .toList();
    }

    /** One displayed value per attribute: most reliable source first, then newest. */
    private List<FactTO> resolveFacts(List<Statement> statements) {
        Comparator<Statement> best = Comparator
                .comparingInt((Statement s) -> dataset.reliability().getOrDefault(s.source(), 0)).reversed()
                .thenComparing(Statement::date, Comparator.reverseOrder());
        Map<String, List<Statement>> groups = statements.stream()
                .collect(Collectors.groupingBy(Statement::attribute, LinkedHashMap::new, Collectors.toList()));
        List<FactTO> facts = new ArrayList<>();
        groups.forEach((attribute, group) -> {
            Statement chosen = group.stream().min(best).orElseThrow();
            String value = String.valueOf(chosen.value());
            Attribute definition = dataset.attributes().get(attribute);
            String date = chosen.date();
            int refreshDays = definition == null ? 365 : definition.refreshAfterDays();
            boolean fresh = date.length() >= 10 && LocalDate.parse(date.substring(0, 10)).plusDays(refreshDays).isAfter(LocalDate.now());
            facts.add(new FactTO(attribute, definition == null ? attribute : definition.label(),
                    definition == null ? "" : definition.definition(), chosen.value(), chosen.source(),
                    SOURCE_LABELS.getOrDefault(chosen.source(), chosen.source()), chosen.sourceRef(), date.isEmpty() ? null : date, fresh,
                    chosen.source().equals("crawl"),
                    group.stream().anyMatch(s -> !String.valueOf(s.value()).equals(value)),
                    (int) group.stream().filter(s -> s.source().equals("user") && String.valueOf(s.value()).equals(value)).count()));
        });
        return facts;
    }

    private LocationReviewTO toReview(ReviewEntity review) {
        return new LocationReviewTO(review.getId().getPlaceId() + "|" + review.getId().getUserId(), review.getUser().getUsername(),
                review.getOverallRating(), review.getMessage() == null ? "" : review.getMessage(),
                ticked(review.getPositiveRatings()), ticked(review.getNegativeRatings()), iso(review.getPublishDate()));
    }

    private static List<String> ticked(Map<String, Boolean> ratings) {
        return ratings.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
    }

    private LocationReportTO toReport(ReportEntity report) {
        return new LocationReportTO(report.getId(), report.getType(), report.getDescription(), iso(report.getCreatedAt()), report.getConfirmations());
    }

    private static String iso(LocalDateTime time) {
        Instant instant = time.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toString();
    }

    private ProfileTO toProfile(UserEntity user) {
        String[] words = user.getUsername().trim().split("\\s+");
        String initials = Arrays.stream(words).limit(2).map(w -> w.substring(0, 1).toUpperCase(Locale.ROOT)).collect(Collectors.joining());
        return new ProfileTO(String.valueOf(user.getId()), user.getUsername(), initials, user.getPoints(),
                user.getReviews() == null ? 0 : user.getReviews().size(), (int) reportRepository.countByReporterId(user.getId()));
    }

    private static AchievementTO achievement(String id, String name, String description, String icon, int target, int progress) {
        return new AchievementTO(id, name, description, icon, target, Math.min(progress, target), progress >= target);
    }
}
