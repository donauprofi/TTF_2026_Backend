package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response;

import java.util.List;

public record CatalogTO(List<FeatureTO> amenities, List<FeatureTO> obstacles, List<ReportTypeTO> reportTypes, List<String> attribution) {
}
