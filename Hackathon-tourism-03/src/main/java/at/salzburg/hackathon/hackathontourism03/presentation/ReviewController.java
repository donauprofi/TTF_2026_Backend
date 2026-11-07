package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.CreateReviewTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.ReviewPlaceTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.ReviewTO;
import at.salzburg.hackathon.hackathontourism03.service.logics.ReviewLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReviewController {
    @Autowired
    private ReviewLogic reviewLogic;

    @PostMapping("createReview")
    public ResponseEntity<Void> createReview(@RequestBody CreateReviewTO input) {
        boolean success = reviewLogic.createReview(input.getId(), input.getLocationName(), input.getMessage(), input.getPositiveRatings(), input.getNegativeRatings(), input.getOverallRating());
        if(!success) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.status(201).build();
    }

    @GetMapping("getAllReviewsOfUser")
    public ResponseEntity<ReviewTO> getAllReviewsOfUser(@RequestHeader Long id) {
        ReviewTO result = reviewLogic.getAllReviewsOfUser(id);
        if(result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("editReview")
    public ResponseEntity<Void> editReview(@RequestBody CreateReviewTO input) {
        boolean success = reviewLogic.editReview(input.getId(), input.getLocationName(), input.getMessage(), input.getPositiveRatings(), input.getNegativeRatings(), input.getOverallRating());
        if(!success) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.status(202).build();
    }

    @GetMapping("getAllReviewsOfPlace")
    public ResponseEntity<ReviewPlaceTO> getAllReviewsOfPlace(@RequestHeader String place) {
        ReviewPlaceTO result = reviewLogic.getAllReviewsOfPlace(place);
        if(result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }
}
