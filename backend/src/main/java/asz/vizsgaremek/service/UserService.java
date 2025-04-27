package asz.vizsgaremek.service;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.converter.UserConverter;
import asz.vizsgaremek.dto.user.PictureRead;
import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.enums.Role;
import asz.vizsgaremek.exception.UserNotFoundException;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ContactRepository;
import asz.vizsgaremek.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
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
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private ContactRepository contactRepository;



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
        user.setPicture("/images/basic/basic.png");
        User createdUser = repository.save(user);
        return UserConverter.convertModelToRead(createdUser);
    }


    private void throwExceptionIfUserNotFound(int id){
        if (!repository.existsById(id)) throw new UserNotFoundException();
    }

    public UserRead deleteUser(int id) {
        throwExceptionIfUserNotFound(id);
        User deletedUser = repository.getReferenceById(id);
        contactRepository.deleteContactByUserId(deletedUser.getId());
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
        System.out.println(userSave.getRole());
        User user = UserConverter.convertSaveToModel(id,userSave);
        User updatedUser = repository.save(user);
        return UserConverter.convertModelToRead(updatedUser);
    }



    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(String username) {
        return repository.findByUserName(username)
                .orElseThrow(UserNotFoundException::new);
    }

    public User findById(Integer id){
        return repository.findById(id)
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
        user.setPicture("/images/basic/basic.png");
        user.setRole(Role.USER);
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
        String uploadDir = "uploads/";
        String subFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        Path fullPath = Paths.get(uploadDir, subFolderName);

        // Mappa létrehozása, ha nem létezik
        try {
            Files.createDirectories(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Nem sikerült létrehozni a célmappát", e);
        }

        // Alapértelmezett profilkép útvonala
        String defaultProfilePicture = "/images/basic/basic.png";
        String defaultProfileStoragePath = "uploads/basic/basic.png"; // Fizikai fájl elérési útja

        // Lekérdezzük a felhasználó jelenlegi képének elérési útját
        User user = readUserEntity(userId);
        String oldPicturePath = user.getPicture(); // Pl. "/images/2025-03-30/kep.png"

        // Ha van korábbi kép és NEM a default, akkor töröljük
        if (oldPicturePath != null && !oldPicturePath.isEmpty()) {
            Path oldFilePath = Paths.get(uploadDir, oldPicturePath.replace("/images/", ""));

            // **Csak akkor töröljük, ha nem a default kép**
            if (!oldPicturePath.equals(defaultProfilePicture) && !oldFilePath.toString().equals(defaultProfileStoragePath)) {
                try {
                    Files.deleteIfExists(oldFilePath);
                } catch (IOException e) {
                    System.err.println("Nem sikerült törölni a régi képet: " + oldFilePath);
                }
            }
        }

        // Új fájlnév generálása
        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String baseFileName = StringUtils.stripFilenameExtension(file.getOriginalFilename());

        if (fileExtension == null || baseFileName == null) {
            throw new RuntimeException("Érvénytelen fájlnév");
        }

        String uniqueFileName = baseFileName + "-" + UUID.randomUUID() + "." + fileExtension;
        Path destinationFilePath = fullPath.resolve(uniqueFileName);

        // Fájl mentése
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Hiba történt a fájl mentésekor", ex);
        }

        // Új kép elérési út frissítése az adatbázisban
        String newPicturePath = "/images/" + subFolderName + "/" + uniqueFileName;
        updateUserPicture(userId, newPicturePath);

        PictureRead pictureRead = new PictureRead();
        pictureRead.setId(user.getId());
        pictureRead.setFullPath(newPicturePath);

        return pictureRead;
    }

    public String getUserPicturePath(Integer userId) {
        return repository.findById(userId)
                .map(User::getPicture)
                .orElse(null);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return repository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}