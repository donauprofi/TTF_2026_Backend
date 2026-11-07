package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewPlaceTO {
    private String user;
    private List<AspectRatingTO> positiveAspects;
    private List<AspectRatingTO> negativeAspects;
    private String message;
    private LocalDateTime creationDate;
    private LocalDateTime lastEditedDate;
    private Integer overallRating;
}
