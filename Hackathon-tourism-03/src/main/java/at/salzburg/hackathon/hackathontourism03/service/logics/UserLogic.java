package at.salzburg.hackathon.hackathontourism03.service.logics;

import at.salzburg.hackathon.hackathontourism03.domain.UserEntity;
import at.salzburg.hackathon.hackathontourism03.enums.Disabilities;
import at.salzburg.hackathon.hackathontourism03.persistence.UserRepository;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.SignupTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.UserTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserLogic {
    @Autowired
    private UserRepository userRepository;

    public List<UserTO> getAllUsers() {
        try {
            List<UserEntity> users = userRepository.findAll();
            List<UserTO> result = new ArrayList<>();
            for(var user : users) {
                result.add(new UserTO(user.getId(), user.getUsername(), user.getReviews().size(), user.getPoints(), user.getDisabilities() != null ? user.getDisabilities().stream().map(Enum::name).collect(Collectors.toSet()) : Set.of()));
            }

            return result;
        } catch (Exception e) {
            System.out.println("Some error occured when trying to fetch all users. Strack trace:");
            e.printStackTrace();
            return null;
        }
    }

    public boolean signup(SignupTO userTO) {
        UserEntity user = new UserEntity();
        user.setUsername(userTO.getUsername());
        user.setPassword(userTO.getPassword());
        Set<Disabilities> disabilities = new HashSet<>();
        for(var disability : userTO.getDisabilities()) {
            try{
                disabilities.add(Disabilities.valueOf(disability));
            } catch(IllegalArgumentException e) {
                System.out.println(disability + " is not a known disability");
                return false;
            }
        }

        user.setDisabilities(disabilities);
        userRepository.save(user);
        return true;
    }
}
