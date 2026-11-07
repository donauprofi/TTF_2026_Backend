package at.salzburg.hackathon.hackathontourism03.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "Review")
@Data
public class ReviewEntity {

    @EmbeddedId
    private ReviewId id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @MapsId("placeId")
    @JoinColumn(name = "place_id")
    private PlaceEntity place;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column
    @NotNull
    private LocalDateTime publishDate;

    @Column
    private LocalDateTime lastEditedDate;

    @ElementCollection
    @CollectionTable(
            name = "positive_review_ratings",
            joinColumns = {
                    @JoinColumn(
                            name = "positive_review_place_id",
                            referencedColumnName = "place_id"
                    ),
                    @JoinColumn(
                            name = "positive_review_user_id",
                            referencedColumnName = "user_id"
                    )
            }
    )
    @MapKeyColumn(name = "positive_rating_key")
    @Column(name = "positive_rating_value")
    private Map<String, Boolean> positiveRatings = new HashMap<>();

    @ElementCollection
    @CollectionTable(
            name = "negative_review_ratings",
            joinColumns = {
                    @JoinColumn(
                            name = "negative_review_place_id",
                            referencedColumnName = "place_id"
                    ),
                    @JoinColumn(
                            name = "negative_review_user_id",
                            referencedColumnName = "user_id"
                    )
            }
    )
    @MapKeyColumn(name = "negative_rating_key")
    @Column(name = "negative_rating_value")
    private Map<String, Boolean> negativeRatings = new HashMap<>();

    @Column
    private String message;

    @Column
    private Integer overallRating;
}
