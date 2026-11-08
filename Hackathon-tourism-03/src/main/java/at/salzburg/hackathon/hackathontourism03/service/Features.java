package at.salzburg.hackathon.hackathontourism03.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Each chip is one (attribute, value) pair from the dataset vocabulary, so a review chip
 * can be treated as a fact and a fact can be displayed as a chip.
 */
public final class Features {
    public record Feature(String id, String label, String icon, String attribute, String value) {
    }

    public static final List<Feature> AMENITIES = List.of(
            new Feature("wheelchair", "Wheelchair accessible", "Accessibility", "wheelchair_access", "yes"),
            new Feature("ramp", "Step-free entrance", "Accessibility", "step_free_entrance", "yes"),
            new Feature("wheelchair-ramp", "Ramp", "Accessibility", "ramp", "yes"),
            new Feature("accessible-entrance", "Accessible entrance", "DoorOpen", "accessible_entrance", "yes"),
            new Feature("wide-doors", "Wide doors and corridors", "DoorOpen", "wide_doors", "yes"),
            new Feature("automatic-door", "Automatic door", "DoorOpen", "automatic_door", "yes"),
            new Feature("elevator", "Accessible lift", "ArrowUpDown", "accessible_lift", "yes"),
            new Feature("toilet", "Accessible restroom", "PersonStanding", "accessible_toilet", "yes"),
            new Feature("accessible-bathroom", "Accessible bathroom", "PersonStanding", "accessible_bathroom", "yes"),
            new Feature("accessible-rooms", "Accessible rooms", "Hotel", "accessible_rooms", "yes"),
            new Feature("outdoor-area", "Accessible terrace or garden", "Trees", "accessible_outdoor_area", "yes"),
            new Feature("parking", "Accessible parking", "Accessibility", "accessible_parking", "yes"),
            new Feature("handrail", "Handrails", "Hand", "handrail", "yes"),
            new Feature("tactile", "Tactile paving", "Footprints", "tactile_paving", "yes"),
            new Feature("audio", "Audio assistance", "AudioLines", "audio_assistance", "yes"),
            new Feature("quiet", "Quiet space", "VolumeX", "quiet_environment", "yes"),
            new Feature("sign-language", "Sign language", "Hand", "sign_language", "yes"),
            new Feature("pushchair", "Pushchair friendly", "Baby", "pushchair_friendly", "yes"),
            new Feature("companion", "Support for companions", "HeartHandshake", "companion_support", "yes"));

    public static final List<Feature> OBSTACLES = List.of(
            new Feature("wheelchair-limited", "Limited wheelchair access", "Accessibility", "wheelchair_access", "limited"),
            new Feature("no-wheelchair", "Not wheelchair accessible", "Accessibility", "wheelchair_access", "no"),
            new Feature("steps", "Steps at entrance", "Footprints", "step_free_entrance", "no"),
            new Feature("no-ramp", "No ramp", "Accessibility", "ramp", "no"),
            new Feature("narrow", "Narrow doorways", "DoorOpen", "wide_doors", "no"),
            new Feature("elevator-limited", "Lift only partly accessible", "ArrowUpDown", "accessible_lift", "limited"),
            new Feature("no-elevator", "No accessible lift", "ArrowUpDown", "accessible_lift", "no"),
            new Feature("no-toilet", "No accessible restroom", "PersonStanding", "accessible_toilet", "no"),
            new Feature("no-handrail", "No handrails", "Hand", "handrail", "no"),
            new Feature("noise", "Loud environment", "Volume2", "quiet_environment", "no"));

    private Features() {
    }

    private static List<Feature> all() {
        List<Feature> features = new ArrayList<>(AMENITIES);
        features.addAll(OBSTACLES);
        return features;
    }

    public static Optional<Feature> byId(String id) {
        return all().stream().filter(f -> f.id().equals(id)).findFirst();
    }

    public static Optional<Feature> byFact(String attribute, String value) {
        return all().stream().filter(f -> f.attribute().equals(attribute) && f.value().equals(value)).findFirst();
    }

    public static boolean isAmenity(Feature feature) {
        return AMENITIES.contains(feature);
    }

    public static List<String> conflictsOf(Feature amenity) {
        return OBSTACLES.stream().filter(o -> o.attribute().equals(amenity.attribute())).map(Feature::id).toList();
    }
}
