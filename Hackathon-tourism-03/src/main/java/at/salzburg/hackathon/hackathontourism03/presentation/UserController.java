package at.salzburg.hackathon.hackathontourism03.presentation;

import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive.SignupTO;
import at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.response.UserTO;
import at.salzburg.hackathon.hackathontourism03.service.logics.UserLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("signup")
    public ResponseEntity<Void> signup(@RequestBody SignupTO user) {
        boolean success = userLogic.signup(user);
        if(!success) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.status(201).build();
    }

}
