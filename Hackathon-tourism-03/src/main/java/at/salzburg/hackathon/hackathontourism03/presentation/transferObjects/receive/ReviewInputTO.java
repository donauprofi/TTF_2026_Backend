package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive;

import lombok.Data;

import java.util.List;

@Data
public class ReviewInputTO {
    private Integer rating;
    private List<String> amenities;
    private List<String> obstacles;
    private String text;
}
