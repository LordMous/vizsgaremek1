package asz.vizsgaremek.controller;

import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "User functions", description = "Here you can manage the users")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
@CrossOrigin("http://localhost:5173")
public class UserController {

    @Autowired
    private UserService service;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/list")
    @Operation(summary = "List of users",description = "List of users")
    public List<UserRead> listAllUsers() {
        return service.getAllUsers();
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new user")
    public UserRead createUser(@RequestBody UserSave item) {return service.createUser(item);}


    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("{id}")
    @Operation(summary = "Read selected user")
    public UserRead readUser(@PathVariable int id){return service.readUser(id);}



    @CrossOrigin(origins = "http://localhost:5173")
    @PutMapping("/{id}")
    @Operation(summary = "Update selected user")
    public UserRead updateUser(@PathVariable int id, @RequestBody @Valid UserSave userSave){
        return service.updateUser(id, userSave);
    }


    @CrossOrigin(origins = "http://localhost:5173")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete selected user")
    public UserRead deleteUser(@PathVariable int id) {
        return service.deleteUser(id);
    }


}
