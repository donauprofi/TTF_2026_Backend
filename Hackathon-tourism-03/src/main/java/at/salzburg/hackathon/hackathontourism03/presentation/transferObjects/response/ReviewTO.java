package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewTO {
    private String place;
    private List<AspectRatingTO> positiveAspects;
    private List<AspectRatingTO> negativeAspects;
    private String message;
    private LocalDateTime creationDate;
    private LocalDateTime lastEditedDate;
    private Integer overallRating;
}
