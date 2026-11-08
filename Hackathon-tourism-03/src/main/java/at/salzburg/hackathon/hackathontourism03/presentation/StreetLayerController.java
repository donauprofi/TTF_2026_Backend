package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.StreetLayerTO;
import at.salzburg.hackathon.hackathontourism03.service.StreetLayerDataset;
import at.salzburg.hackathon.hackathontourism03.service.logics.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StreetLayerController {
    private static final int MAX_FEATURES = 4000;

    @Autowired
    private StreetLayerDataset streetLayer;

    /** @param bbox minLng,minLat,maxLng,maxLat */
    @GetMapping("street-layer")
    public StreetLayerTO getStreetLayer(@RequestParam String bbox) {
        if (!streetLayer.isLoaded()) {
            throw new ApiException(503, "The street layer is not available right now.");
        }
        double[] box = parse(bbox);
        List<JsonNode> features = streetLayer.within(box[0], box[1], box[2], box[3], MAX_FEATURES + 1);
        boolean truncated = features.size() > MAX_FEATURES;
        return new StreetLayerTO("FeatureCollection", truncated ? features.subList(0, MAX_FEATURES) : features, truncated,
                StreetLayerDataset.ATTRIBUTION, StreetLayerDataset.SOURCE_URL, StreetLayerDataset.LICENSE_URL);
    }

    private static double[] parse(String bbox) {
        String[] parts = bbox.split(",");
        try {
            if (parts.length == 4) {
                double[] box = new double[4];
                for (int i = 0; i < 4; i++) box[i] = Double.parseDouble(parts[i]);
                return box;
            }
        } catch (NumberFormatException e) {
            // Reported below with the same message as a wrong number of values.
        }
        throw new ApiException(400, "Use bbox=minLng,minLat,maxLng,maxLat.");
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiError(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }
}
