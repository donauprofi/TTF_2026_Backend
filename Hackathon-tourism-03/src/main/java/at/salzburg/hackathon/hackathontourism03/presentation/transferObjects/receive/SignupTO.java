package at.salzburg.hackathon.hackathontourism03.presentation.transferObjects.receive;

import lombok.Data;

import java.util.List;

@Data
public class SignupTO {
    private String username;

    private String password;

    private List<String> disabilities;
}
