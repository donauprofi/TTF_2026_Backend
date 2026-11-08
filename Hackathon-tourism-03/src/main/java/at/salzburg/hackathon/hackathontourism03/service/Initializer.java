package at.salzburg.hackathon.hackathontourism03.service;

import at.salzburg.hackathon.hackathontourism03.domain.PlaceEntity;
import at.salzburg.hackathon.hackathontourism03.domain.UserEntity;
import at.salzburg.hackathon.hackathontourism03.persistence.PlaceRepository;
import at.salzburg.hackathon.hackathontourism03.persistence.UserRepository;
import at.salzburg.hackathon.hackathontourism03.service.logics.LocationLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class Initializer implements ApplicationRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        UserEntity user = new UserEntity();
        user.setUsername("Goofy");
        userRepository.save(user);
        UserEntity demoUser = new UserEntity();
        demoUser.setUsername(LocationLogic.DEMO_USERNAME);
        userRepository.save(demoUser);
        PlaceEntity place = new PlaceEntity();
        place.setLocationName("Alterlaa");
        placeRepository.save(place);
    }
}
