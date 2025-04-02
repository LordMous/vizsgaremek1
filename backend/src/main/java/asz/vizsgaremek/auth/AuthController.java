package asz.vizsgaremek.auth;

import asz.vizsgaremek.converter.UserConverter;
import asz.vizsgaremek.dto.user.LoginResponseDTO;
import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:5173") // Engedélyezi a frontendet
public class AuthController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setAge(registerRequest.getAge());
        user.setUserName(registerRequest.getUserName());
        user.setPassword(registerRequest.getPassword());
        user.setPhoneNumber(registerRequest.getPhoneNumber());

        // Átalakítás UserSave-re
        UserSave userSave = UserConverter.convertModelToSave(user);

        // Regisztráció
        UserRead userRead = userService.registerUser(userSave);
        
        return ResponseEntity.ok(userRead);
    }


    /*
    LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(token);  // Token generálása
            response.setUserId(user.getId());
     */




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());

        if (user != null && user.getPassword().equals(request.getPassword())) {
            String token = jwtUtil.generateToken(user.getUserName());

            return ResponseEntity.ok(new AuthResponse(token,user.getId()));
        }
        return ResponseEntity.status(401).body("Hibás email vagy jelszó!");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
        }

        String token = authHeader.substring(7);  // A "Bearer " előtag eltávolítása
        System.out.println("Received token: " + token);  // Token naplózása

        try {
            String username = jwtUtil.extractUsername(token);
            if (username == null || !jwtUtil.validateToken(token, username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
            }

            return ResponseEntity.ok("Welcome to the dashboard, " + username);
        } catch (Exception e) {
            System.err.println("Token processing failed: " + e.getMessage());  // Hiba naplózása
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token processing failed: " + e.getMessage());
        }
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

    public AuthResponse(String token, Integer userId) {
        this.token = token;
        this.userId = userId;
    }
}