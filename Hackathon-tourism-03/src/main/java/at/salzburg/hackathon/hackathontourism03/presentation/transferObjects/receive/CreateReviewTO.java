package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive;

import lombok.Data;

import java.util.Map;

@Data
public class CreateReviewTO {
    private Long id;
    private String locationName;
    private String message;
    private Integer overallRating;
    private Map<String, Boolean> positiveRatings;
    private Map<String, Boolean> negativeRatings;
}
