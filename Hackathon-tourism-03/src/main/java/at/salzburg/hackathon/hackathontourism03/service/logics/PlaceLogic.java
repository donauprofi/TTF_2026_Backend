package at.salzburg.hackathon.hackathontourism03.service.logics;

import at.salzburg.hackathon.hackathontourism03.domain.PlaceEntity;
import at.salzburg.hackathon.hackathontourism03.persistence.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlaceLogic {
    @Autowired
    private PlaceRepository placeRepository;


    public boolean addPlace(String locationName) {
        PlaceEntity place = new PlaceEntity();
        place.setLocationName(locationName);
        placeRepository.save(place);
        return true;
    }
}
