package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AspectRatingTO {
    private String aspect;

    private Boolean ticked;
}
