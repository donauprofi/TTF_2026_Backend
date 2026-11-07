package at.salzburg.hackathon.hackathontourism03.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
            name = "review_ratings",
            joinColumns = {
                    @JoinColumn(
                            name = "review_place_id",
                            referencedColumnName = "place_id"
                    ),
                    @JoinColumn(
                            name = "review_user_id",
                            referencedColumnName = "user_id"
                    )
            }
    )
    @MapKeyColumn(name = "rating_key")
    @Column(name = "rating_value")
    private Map<String, Integer> ratings = new HashMap<>();

    @Column
    private String message;
}
