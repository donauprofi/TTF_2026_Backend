package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.UserTO;
import at.salzburg.hackathon.hackathontourism03.service.logics.UserLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserLogic userLogic;

    @GetMapping("/getAllUsers")
    public ResponseEntity<List<UserTO>> getAllUsers() {
        List<UserTO> result = userLogic.getAllUsers();
        if(result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

//    @PostMapping("signup") {
//
//    }
}
