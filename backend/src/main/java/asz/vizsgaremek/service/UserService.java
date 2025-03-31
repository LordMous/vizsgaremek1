package asz.vizsgaremek.service;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.converter.UserConverter;
import asz.vizsgaremek.dto.user.PictureRead;
import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.exception.UserNotFoundException;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // Jelszó titkosítás



    public List<UserRead> getAllUsers() {
        List<User> users = repository.findAll();
        return users.stream()
                .map(UserConverter::convertModelToRead)
                .toList();
    }

    public UserRead createUser(UserSave userSave){
        User user = UserConverter.convertSaveToModel(userSave);
        User createdUser = repository.save(user);
        return UserConverter.convertModelToRead(createdUser);
    }


    private void throwExceptionIfUserNotFound(int id){
        if (!repository.existsById(id)) throw new UserNotFoundException();
    }

    public UserRead deleteUser(int id) {
        throwExceptionIfUserNotFound(id);
        User deletedUser = repository.getReferenceById(id);
        repository.delete(deletedUser);
        return UserConverter.convertModelToRead(deletedUser);
    }

    public UserRead readUser(int id){
        throwExceptionIfUserNotFound(id);
        User user = repository.getReferenceById(id);
        return UserConverter.convertModelToRead(user);
    }

    public UserRead updateUser(int id, @Valid UserSave userSave){
        throwExceptionIfUserNotFound(id);
        User user = UserConverter.convertSaveToModel(id,userSave);
        User updatedUser = repository.save(user);
        return UserConverter.convertModelToRead(updatedUser);
    }



    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    // Regisztráció (jelszó titkosításával)
    public UserRead registerUser(UserSave userSave) {
        User user = new User();
        user.setEmail(userSave.getEmail());
        user.setAge(userSave.getAge());
        user.setUserName(userSave.getUserName());
        user.setPassword(passwordEncoder.encode(userSave.getPassword())); // Jelszó titkosítás
        user.setPhoneNumber(userSave.getPhoneNumber());

        User createdUser = repository.save(user);

        // Átalakítás UserRead-re
        UserRead userRead = new UserRead();
        userRead.setId(createdUser.getId());
        userRead.setEmail(createdUser.getEmail());
        userRead.setAge(createdUser.getAge());
        userRead.setUserName(createdUser.getUserName());
        userRead.setPhoneNumber(createdUser.getPhoneNumber());

        return userRead;
    }

    // Bejelentkezés ellenőrzése
    public String login(String email, String password) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Hibás jelszó!");
        }
        return jwtUtil.generateToken(user.getEmail()); // JWT token generálása
    }


    // Új metódus, amely a User entitást adja vissza
    public User readUserEntity(Integer userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    public void updateUserPicture(Integer id, String pic){
        repository.updateUserPic(id,pic);
    }

    public PictureRead store(MultipartFile file, Integer userId) {
        String rootFolder = "src/main/resources/static/";
        String subFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File fullPath = new File(rootFolder + subFolderName);
        String fullFolderName = rootFolder + subFolderName;
        if (!fullPath.exists()) {
            if (!fullPath.mkdirs()) {
                fullFolderName = rootFolder;
            }
        }
        String fileNameUniquePart = '-' + new SimpleDateFormat("HH-mm-ss").format(new Date()) + '-'+ (int)(Math.random() * 1000);
        String fileName = file.getOriginalFilename().split("\\.")[0];
        String fileExtension = file.getOriginalFilename().split("\\.")[1];
        String savingFileName = fileName + fileNameUniquePart + '.' + fileExtension;

        Path destinationFilePath = Paths.get(fullFolderName , savingFileName);

        // try with resources
        try (InputStream inputStream = file.getInputStream()){
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.out.println("Hiba: " + ex.getMessage());
        }

        updateUserPicture(userId, savingFileName);
        User user = readUserEntity(userId);
        PictureRead pictureRead = new PictureRead();
        pictureRead.setId(user.getId());
        pictureRead.setFullPath(user.getPicture());
        return pictureRead;
    }
}