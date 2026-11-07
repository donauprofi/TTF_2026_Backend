package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.service.logics.PlaceLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaceController {
    @Autowired
    private PlaceLogic placeLogic;

    @PostMapping("addPlace")
    public ResponseEntity<Void> addPlace(String locationName) {
        boolean result = placeLogic.addPlace(locationName);
        if(!result) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.status(201).build();
    }
}
