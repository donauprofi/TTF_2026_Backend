package at.salzburg.hackathon.hackathontourism03.persistence;

import at.salzburg.hackathon.hackathontourism03.domain.ReviewEntity;
import at.salzburg.hackathon.hackathontourism03.domain.ReviewId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, ReviewId> {
    ReviewEntity findReviewById(ReviewId id);
}
