package at.salzburg.hackathon.hackathontourism03.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Read-only places, facts and vocabulary, loaded from the data pipeline's output files. */
@Component
public class PlaceDataset {
    public record Statement(String attribute, Object value, String source, String sourceRef, String observedAt, String retrievedAt) {
        public String date() {
            return observedAt != null ? observedAt : retrievedAt != null ? retrievedAt : "";
        }
    }

    public record Attribute(String label, String definition, int refreshAfterDays) {
    }

    public record ReportType(String id, String label, int expiresAfterDays) {
    }

    public record BasePlace(String id, String name, String kind, String category, String address, double lat, double lng,
                            String distance, String color, String website, String openingHours,
                            List<Statement> statements, double metres, String searchText) {
    }

    // Gmunden town centre: the starting point used for distance labels.
    private static final double ORIGIN_LAT = 47.9186;
    private static final double ORIGIN_LNG = 13.7995;

    private static final Map<String, Set<String>> KINDS = Map.of(
            "Hotels", Set.of("hotel", "guest_house", "apartment", "camp_site", "alpine_hut"),
            "Food & drink", Set.of("restaurant", "cafe", "bar", "fast_food", "bakery"),
            "Shopping", Set.of("supermarket", "shop"),
            "Outdoors", Set.of("trailhead", "viewpoint", "leisure", "playground", "attraction", "drinking_water"),
            "Health", Set.of("pharmacy", "doctor", "hospital"));
    private static final Map<String, String> COLORS = Map.of(
            "Hotels", "blue",
            "Food & drink", "green",
            "Shopping", "peach",
            "Outdoors", "green",
            "Health", "lavender",
            "Transport", "blue",
            "Culture & services", "lavender");

    @Value("${app.data-dir}")
    private String dataDir;

    private final Map<String, BasePlace> places = new LinkedHashMap<>();
    private final Map<String, Attribute> attributes = new HashMap<>();
    private final Map<String, Integer> reliability = new HashMap<>();
    private final List<ReportType> reportTypes = new ArrayList<>();
    private final List<String> attribution = new ArrayList<>();
    private boolean loaded;

    @PostConstruct
    void load() {
        try {
            JsonMapper mapper = JsonMapper.builder().build();
            JsonNode vocabulary = mapper.readTree(new File(dataDir, "vocabulary.json"));
            for (JsonNode a : vocabulary.get("attributes")) {
                attributes.put(a.get("id").asString(), new Attribute(a.get("label").get("en").asString(),
                        a.path("definition").path("en").asString(""), a.path("refresh_after_days").asInt(365)));
            }
            for (JsonNode r : vocabulary.get("report_types")) {
                reportTypes.add(new ReportType(r.get("id").asString(), r.get("label").get("en").asString(),
                        r.path("expires_after_days").asInt(3)));
            }
            JsonNode sources = mapper.readTree(new File(dataDir, "meta.json")).get("sources");
            for (String name : sources.propertyNames()) {
                JsonNode source = sources.get(name);
                reliability.put(name, source.path("reliability").asInt(0));
                if (source.hasNonNull("attribution")) attribution.add(source.get("attribution").asString());
            }
            for (JsonNode feature : mapper.readTree(new File(dataDir, "places.geojson")).get("features")) {
                BasePlace place = toBasePlace(feature);
                if (place != null && places.put(place.id(), place) != null) {
                    throw new IllegalStateException("Duplicate place id " + place.id());
                }
            }
            loaded = true;
            System.out.println("Loaded " + places.size() + " places from " + dataDir);
        } catch (RuntimeException e) {
            // The original endpoints work without place data, so the app still starts.
            System.out.println("Could not load place data from " + dataDir + ": " + e.getMessage());
        }
    }

    private BasePlace toBasePlace(JsonNode feature) {
        JsonNode p = feature.get("properties");
        String category = categoryOf(p.path("kind").asString(""), p.path("layer").asString(""));
        String name = text(p, "name");
        if (category == null || name == null) return null;
        double lng = feature.get("geometry").get("coordinates").get(0).asDouble();
        double lat = feature.get("geometry").get("coordinates").get(1).asDouble();
        double metres = metresFromOrigin(lat, lng);
        String address = formatAddress(p.path("address"));
        List<Statement> statements = new ArrayList<>();
        for (JsonNode f : p.path("facts")) {
            JsonNode value = f.get("value");
            statements.add(new Statement(f.get("attribute").asString(), value.isNumber() ? value.numberValue() : value.asString(),
                    f.get("source").asString(), text(f, "source_ref"), text(f, "observed_at"), text(f, "retrieved_at")));
        }
        return new BasePlace(
                // The frontend splits its hash route on "/", so public ids cannot contain one.
                p.get("id").asString().replace("/", "_"),
                name, p.get("kind").asString(), category, address, lat, lng, formatDistance(metres), COLORS.get(category),
                text(p.path("contact"), "website"), text(p, "opening_hours"), statements, metres, normalize(name + " " + address));
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.asString().isBlank() ? null : value.asString();
    }

    private static String categoryOf(String kind, String layer) {
        for (var entry : KINDS.entrySet()) {
            if (entry.getValue().contains(kind)) return entry.getKey();
        }
        if (layer.equals("transport")) return "Transport";
        return layer.equals("poi") ? "Culture & services" : null;
    }

    private static String formatAddress(JsonNode address) {
        String line = joinPresent(" ", text(address, "street"), text(address, "housenumber"));
        String full = joinPresent(", ", line.isEmpty() ? null : line, text(address, "city"));
        return full.isEmpty() ? "Traunsee-Almtal" : full;
    }

    private static String joinPresent(String separator, String... parts) {
        return Stream.of(parts).filter(part -> part != null).collect(Collectors.joining(separator));
    }

    private static double metresFromOrigin(double lat, double lng) {
        double dLat = Math.toRadians(lat - ORIGIN_LAT);
        double dLng = Math.toRadians(lng - ORIGIN_LNG);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(ORIGIN_LAT)) * Math.cos(Math.toRadians(lat)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * 6_371_000 * Math.asin(Math.sqrt(a));
    }

    private static String formatDistance(double metres) {
        return metres < 1000
                ? Math.round(metres / 10) * 10 + " m"
                : String.format(Locale.ROOT, "%.1f km", metres / 1000);
    }

    public static String normalize(String value) {
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT).replace("ß", "ss"), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    public boolean isLoaded() {
        return loaded;
    }

    public Map<String, BasePlace> places() {
        return places;
    }

    public Map<String, Attribute> attributes() {
        return attributes;
    }

    public Map<String, Integer> reliability() {
        return reliability;
    }

    public List<ReportType> reportTypes() {
        return reportTypes;
    }

    public List<String> attribution() {
        return attribution;
    }
}
