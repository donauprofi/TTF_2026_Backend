package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

public record FactTO(String attribute, String label, String definition, Object value, String source, String sourceLabel, String sourceRef, String observedAt, boolean fresh, boolean unverified, boolean conflict, int confirmations) {
}
