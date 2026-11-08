package at.salzburg.hackathon.hackathontourism03.persistence;

import at.salzburg.hackathon.hackathontourism03.domain.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, String> {
    List<ReportEntity> findByPlaceIdInAndResolvedAtIsNull(Collection<String> placeIds);

    long countByReporterId(Long reporterId);

    @Query("select distinct r.placeId from ReportEntity r where r.resolvedAt is null")
    List<String> findReportedPlaceIds();
}
