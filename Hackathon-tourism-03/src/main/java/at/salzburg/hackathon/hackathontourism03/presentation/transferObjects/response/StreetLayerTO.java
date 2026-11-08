package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record StreetLayerTO(String type, List<JsonNode> features, boolean truncated, String attribution, String sourceUrl, String licenseUrl) {
}
