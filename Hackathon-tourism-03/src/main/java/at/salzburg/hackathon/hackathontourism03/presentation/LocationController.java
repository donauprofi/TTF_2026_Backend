package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.ReportInputTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.ReviewInputTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.VoteTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.*;
import at.salzburg.hackathon.hackathontourism03.service.logics.ApiException;
import at.salzburg.hackathon.hackathontourism03.service.logics.LocationLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Implements the API contract the frontend expects (docs/api-contract.md in TTF_2026_Frontend). */
@RestController
@RequestMapping("/api")
public class LocationController {
    @Autowired
    private LocationLogic locationLogic;

    @GetMapping("locations")
    public List<LocationTO> getLocations(@RequestParam(defaultValue = "") String q) {
        return locationLogic.getLocations(q);
    }

    @GetMapping("locations/{id}")
    public LocationTO getLocation(@PathVariable String id) {
        return locationLogic.getLocation(id);
    }

    @GetMapping("catalog")
    public CatalogTO getCatalog() {
        return locationLogic.getCatalog();
    }

    @GetMapping("me")
    public ProfileTO getProfile() {
        return locationLogic.getProfile();
    }

    @GetMapping("me/achievements")
    public List<AchievementTO> getAchievements() {
        return locationLogic.getAchievements();
    }

    @PostMapping("locations/{id}/reviews")
    public ReviewResultTO addReview(@PathVariable String id, @RequestBody ReviewInputTO input) {
        return locationLogic.addReview(id, input);
    }

    @PostMapping("locations/{id}/reports")
    public LocationTO addReport(@PathVariable String id, @RequestBody ReportInputTO input) {
        return locationLogic.addReport(id, input);
    }

    @PostMapping("locations/{id}/reports/{reportId}/votes")
    public LocationTO voteReport(@PathVariable String id, @PathVariable String reportId, @RequestBody VoteTO vote) {
        return locationLogic.voteReport(id, reportId, vote);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiError(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody() {
        return ResponseEntity.badRequest().body(Map.of("message", "The request could not be read."));
    }
}
