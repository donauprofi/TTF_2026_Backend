package at.salzburg.hackathon.hackathontourism03.persistence;

import at.salzburg.hackathon.hackathontourism03.domain.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<PlaceEntity, String> {

    PlaceEntity findPlaceEntityByLocationName(String locationName);
}
