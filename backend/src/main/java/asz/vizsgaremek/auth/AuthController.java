package asz.vizsgaremek.auth;

import asz.vizsgaremek.converter.UserConverter;
import asz.vizsgaremek.dto.user.LoginResponseDTO;
import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.enums.Role;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173") // Engedélyezi a frontendet
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    public AuthController(JwtUtil jwtUtil, UserService userService, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setAge(registerRequest.getAge());
        user.setUserName(registerRequest.getUserName());
        user.setPassword(registerRequest.getPassword());
        user.setPhoneNumber(registerRequest.getPhoneNumber());

        user.setPicture("/images/basic/basic.png");

        // Átalakítás UserSave-re
        UserSave userSave = UserConverter.convertModelToSave(user);

        // Regisztráció
        UserRead userRead = userService.registerUser(userSave);
        
        return ResponseEntity.ok(userRead);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user != null && user.getPassword().equals(request.getPassword()) || user != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUserName());
            return ResponseEntity.ok(new AuthResponse(token,user.getId(),user.getRole()));
        }
        return ResponseEntity.status(401).body("Hibás email vagy jelszó!");
    }

}

class LoginRequest {
    private String email;
    private String password;

    // getterek és setterek

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

@Getter
class RegisterRequest {
    private String email;
    private Integer age;
    private String userName;
    private String password;
    private String phoneNumber;

}

@Getter
@Setter
class AuthResponse {
    private String token;
    private Integer userId;
    private Role role;
    public AuthResponse(String token, Integer userId, Role role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }
}