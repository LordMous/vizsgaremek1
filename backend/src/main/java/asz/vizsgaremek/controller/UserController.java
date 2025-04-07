package asz.vizsgaremek.controller;

import asz.vizsgaremek.dto.user.PictureRead;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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


    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping(value = "upload-picture/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a picture of a selected user")
    public PictureRead uploadPicture(@RequestBody MultipartFile file, @PathVariable Integer id) {
        return service.store(file, id);
    }


    @GetMapping("/{userId}/picture")
    @Operation(summary = "Read a picture of a selected user")
    public ResponseEntity<?> getUserPicture(@PathVariable Integer userId) {
        String picturePath = service.getUserPicturePath(userId);

        if (picturePath == null || picturePath.isEmpty()) {
            return ResponseEntity.ok().body(Map.of("hasPicture", false));
        }

        return ResponseEntity.ok().body(Map.of(
                "hasPicture", true,
                "picturePath", picturePath
        ));
    }
}
