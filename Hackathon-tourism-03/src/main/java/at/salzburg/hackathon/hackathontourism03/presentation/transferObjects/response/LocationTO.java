package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import java.util.List;

public record LocationTO(String id, String name, String kind, String category, String address, double lat, double lng, String distance, String tagline, String color, String website, String openingHours, double score, int reviewCount, String wheelchairAccess, List<String> amenities, List<String> obstacles, List<FactTO> facts, List<LocationReviewTO> reviews, List<LocationReportTO> reports) {
}
