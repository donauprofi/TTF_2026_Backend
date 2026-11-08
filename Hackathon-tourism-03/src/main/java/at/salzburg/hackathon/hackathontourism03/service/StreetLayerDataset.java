package at.salzburg.hackathon.hackathontourism03.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Hackathon Veterans street data, licensed CC BY-SA 4.0. It cannot be combined with our ODbL
 * dataset, so it stays a separate read-only layer: never copy it, or anything computed from it,
 * into places, facts, the database or an export.
 */
@Component
public class StreetLayerDataset {
    public static final String ATTRIBUTION = "Street data: Hackathon Veterans, CC BY-SA 4.0. © OpenStreetMap contributors";
    public static final String SOURCE_URL = "https://portal.ttf.nexyo.io/products?id=8beab1aa-496d-4791-bc13-788e3b85e2c8";
    public static final String LICENSE_URL = "https://creativecommons.org/licenses/by-sa/4.0/";

    private record Street(JsonNode feature, double minLng, double minLat, double maxLng, double maxLat) {
    }

    private static final class Bounds {
        double minLng = Double.MAX_VALUE;
        double minLat = Double.MAX_VALUE;
        double maxLng = -Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;

        void include(JsonNode coordinates) {
            if (coordinates.get(0).isNumber()) {
                double lng = coordinates.get(0).asDouble();
                double lat = coordinates.get(1).asDouble();
                minLng = Math.min(minLng, lng);
                minLat = Math.min(minLat, lat);
                maxLng = Math.max(maxLng, lng);
                maxLat = Math.max(maxLat, lat);
                return;
            }
            coordinates.forEach(this::include);
        }
    }

    @Value("${app.street-layer-file}")
    private String file;

    private final List<Street> streets = new ArrayList<>();
    private boolean loaded;

    @PostConstruct
    void load() {
        try {
            JsonNode collection = JsonMapper.builder().build().readTree(new File(file));
            for (JsonNode feature : collection.get("features")) {
                dropNullProperties((ObjectNode) feature.get("properties"));
                Bounds bounds = new Bounds();
                bounds.include(feature.get("geometry").get("coordinates"));
                streets.add(new Street(feature, bounds.minLng, bounds.minLat, bounds.maxLng, bounds.maxLat));
            }
            loaded = true;
            System.out.println("Loaded " + streets.size() + " street features from " + file);
        } catch (RuntimeException e) {
            System.out.println("Street layer is off, could not load " + file + ": " + e.getMessage());
        }
    }

    private static void dropNullProperties(ObjectNode properties) {
        List<String> present = new ArrayList<>();
        for (String name : properties.propertyNames()) {
            if (!properties.get(name).isNull()) present.add(name);
        }
        properties.retain(present);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public List<JsonNode> within(double minLng, double minLat, double maxLng, double maxLat, int limit) {
        return streets.stream()
                .filter(s -> s.minLng() <= maxLng && s.maxLng() >= minLng && s.minLat() <= maxLat && s.maxLat() >= minLat)
                .limit(limit)
                .map(Street::feature)
                .toList();
    }
}
