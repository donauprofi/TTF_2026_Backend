package at.salzburg.hackathon.hackathontourism03.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewId implements Serializable {

    private String placeId;
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReviewId reviewId = (ReviewId) o;
        return Objects.equals(placeId, reviewId.placeId) && Objects.equals(userId, reviewId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId, userId);
    }
}
