package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import java.util.List;

public record LocationReviewTO(String id, String name, int rating, String text, List<String> amenities, List<String> obstacles, String createdAt) {
}
