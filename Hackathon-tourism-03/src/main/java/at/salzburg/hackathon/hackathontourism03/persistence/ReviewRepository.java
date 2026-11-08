package at.salzburg.hackathon.hackathontourism03.persistence;

import at.salzburg.hackathon.hackathontourism03.domain.ReviewEntity;
import at.salzburg.hackathon.hackathontourism03.domain.ReviewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, ReviewId> {
    ReviewEntity findReviewById(ReviewId id);

    List<ReviewEntity> findByIdPlaceIdIn(Collection<String> placeIds);

    @Query("select distinct r.id.placeId from ReviewEntity r")
    List<String> findReviewedPlaceIds();
}
