package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import java.util.List;

public record FeatureTO(String id, String label, String icon, List<String> conflicts) {
}
