package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.CreateReviewTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.ReviewTO;
import at.salzburg.hackathon.hackathontourism03.service.logics.ReviewLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {
    @Autowired
    private ReviewLogic reviewLogic;

    @PostMapping("createReview")
    public HttpStatusCode createReview(@RequestBody CreateReviewTO input) {
        boolean success = reviewLogic.createReview(input.getId(), input.getLocationName(), input.getMessage(), input.getRatings());
        if(!success) {
            return HttpStatusCode.valueOf(400);
        }

        return HttpStatusCode.valueOf(201);
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
    public HttpStatusCode editReview(@RequestBody CreateReviewTO input) {
        boolean success = reviewLogic.editReview(input.getId(), input.getLocationName(), input.getMessage(), input.getRatings());
        if(!success) {
            return HttpStatusCode.valueOf(400);
        }

        return HttpStatusCode.valueOf(202);
    }
}
