package asz.vizsgaremek.dto.user;

import asz.vizsgaremek.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSave {

    @NotNull
    private String userName;
    @NotNull
    private String email;
    @NotNull
    private String phoneNumber;
    @NotNull
    private String password;
    @NotNull
    private Integer age;
    private Role role;
    private String picture;
}
