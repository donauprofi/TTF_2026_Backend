package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

public record AchievementTO(String id, String name, String description, String icon, int target, int progress, boolean unlocked) {
}
