package at.salzburg.hackathon.hackathontourism03.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "Place")
public class PlaceEntity {
    @Id
    private String locationName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "place")
    private List<ReviewEntity> reviews;

    public void addReview(ReviewEntity review) {
        if(reviews == null) {
            reviews = new ArrayList<>();
        }

        reviews.add(review);
        review.setPlace(this);
    }

    public void removeReview(ReviewEntity review) {
        if(reviews == null) {
            return;
        }
         if(reviews.isEmpty()) {
             return;
         }

         reviews.remove(review);
         review.setPlace(null);
    }
}
