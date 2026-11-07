package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import java.util.Set;

public record UserTO(Long id, String username, int numberOfReviews, int points, Set<String> disabilities) {
}
