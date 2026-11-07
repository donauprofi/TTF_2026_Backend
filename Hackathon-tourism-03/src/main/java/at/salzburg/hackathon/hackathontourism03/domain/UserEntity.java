package at.salzburg.hackathon.hackathontourism03.domain;

import at.salzburg.hackathon.hackathontourism03.enums.Disabilities;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.AnyDiscriminatorImplicitValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "AppUser")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @NotNull
    private String username;

    @Column
    private Set<Disabilities> disabilities;

    @Column
    private Integer points = 0;

    @Column
    private String password;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user")
    private List<ReviewEntity> reviews;

    public void addReview(ReviewEntity review) {
        if(reviews == null) {
            reviews = new ArrayList<>();
        }

        reviews.add(review);
        review.setUser(this);
    }

    public void removeReview(ReviewEntity review) {
        if(reviews == null) {
            return;
        }
        if(reviews.isEmpty()) {
            return;
        }

        reviews.remove(review);
        review.setUser(null);
    }
}
