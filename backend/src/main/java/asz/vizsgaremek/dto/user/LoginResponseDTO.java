package asz.vizsgaremek.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {
    private String token;
    private Integer userId;


}