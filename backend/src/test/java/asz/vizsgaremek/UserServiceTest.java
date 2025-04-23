package asz.vizsgaremek;

import asz.vizsgaremek.auth.JwtUtil;
import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.user.UserSave;
import asz.vizsgaremek.exception.UserNotFoundException;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ContactRepository;
import asz.vizsgaremek.repository.UserRepository;
import asz.vizsgaremek.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_ShouldReturnUserRead_WhenValidInput() {
        // Arrange
        UserSave userSave = new UserSave();
        userSave.setEmail("test@example.com");
        userSave.setUserName("testuser");
        userSave.setPassword("password");
        userSave.setPhoneNumber("123456789");
        userSave.setAge(25);

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setEmail(userSave.getEmail());
        savedUser.setUserName(userSave.getUserName());
        savedUser.setPhoneNumber(userSave.getPhoneNumber());
        savedUser.setAge(userSave.getAge());
        savedUser.setPassword("encryptedPassword");

        when(repository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserRead result = userService.registerUser(userSave);

        // Assert
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUserName()).isEqualTo("testuser");
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() {
        // Arrange
        String email = "test@example.com";
        String rawPassword = "secret";
        String encodedPassword = new BCryptPasswordEncoder().encode(rawPassword);
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        when(repository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(email)).thenReturn("mocked-jwt-token");

        // Act
        String token = userService.login(email, rawPassword);

        // Assert
        assertThat(token).isEqualTo("mocked-jwt-token");
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {
        // Arrange
        String email = "test@example.com";
        String correctPassword = "secret";
        String wrongPassword = "wrong";
        User user = new User();
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(correctPassword));

        when(repository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act + Assert
        assertThatThrownBy(() -> userService.login(email, wrongPassword))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Hibás jelszó!");
    }

    @Test
    void deleteUser_ShouldDeleteAndReturnUserRead_WhenUserExists() {
        // Arrange
        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setUserName("test");

        when(repository.existsById(userId)).thenReturn(true);
        when(repository.getReferenceById(userId)).thenReturn(user);

        // Act
        UserRead result = userService.deleteUser(userId);

        // Assert
        verify(contactRepository).deleteContactByUserId(userId);
        verify(repository).delete(user);
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUserName()).isEqualTo("test");
    }

    @Test
    void deleteUser_ShouldThrow_WhenUserDoesNotExist() {
        // Arrange
        int userId = 1;
        when(repository.existsById(userId)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getCurrentAuthenticatedUser_ShouldReturnUser_WhenAuthenticated() {
        // Arrange
        String username = "currentUser";
        User user = new User();
        user.setUserName(username);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        when(repository.findByUserName(username)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getCurrentAuthenticatedUser();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo(username);
    }


    @Test
    void getCurrentAuthenticatedUser_ShouldThrow_WhenUserNotFound() {
        // Arrange
        String username = "missingUser";

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        when(repository.findByUserName(username)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> userService.getCurrentAuthenticatedUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void createUser_ShouldReturnUserRead_WhenValidInput() {
        // Arrange
        UserSave userSave = new UserSave();
        userSave.setUserName("testuser");
        userSave.setEmail("test@example.com");
        userSave.setAge(25);
        userSave.setPhoneNumber("123456789");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setUserName(userSave.getUserName());
        savedUser.setEmail(userSave.getEmail());
        savedUser.setPicture("/images/basic/basic.png");

        when(repository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserRead result = userService.createUser(userSave);

        // Assert
        assertThat(result.getUserName()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }


    @Test
    void updateUser_ShouldReturnUpdatedUserRead_WhenUserExists() {
        // Arrange
        int userId = 1;
        UserSave userSave = new UserSave();
        userSave.setUserName("updatedUser");
        userSave.setEmail("updated@example.com");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUserName(userSave.getUserName());
        updatedUser.setEmail(userSave.getEmail());

        when(repository.existsById(userId)).thenReturn(true);
        when(repository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserRead result = userService.updateUser(userId, userSave);

        // Assert
        assertThat(result.getUserName()).isEqualTo("updatedUser");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        int userId = 1;
        UserSave userSave = new UserSave();

        when(repository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUser(userId, userSave))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void readUser_ShouldReturnUserRead_WhenUserExists() {
        int userId = 1;
        User user = new User();
        user.setId(userId);
        user.setUserName("reader");

        when(repository.existsById(userId)).thenReturn(true);
        when(repository.getReferenceById(userId)).thenReturn(user);

        UserRead result = userService.readUser(userId);

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUserName()).isEqualTo("reader");
    }

    @Test
    void readUser_ShouldThrow_WhenUserNotFound() {
        int userId = 1;
        when(repository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.readUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserReads() {
        User user1 = new User();
        user1.setId(1);
        user1.setUserName("Alice");

        User user2 = new User();
        user2.setId(2);
        user2.setUserName("Bob");

        when(repository.findAll()).thenReturn(List.of(user1, user2));

        List<UserRead> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getUserName()).isEqualTo("Alice");
        assertThat(users.get(1).getUserName()).isEqualTo("Bob");
    }

    @Test
    void getUserPicturePath_ShouldReturnPicturePath_WhenUserExists() {
        int userId = 1;
        String picturePath = "/images/test/pic.png";
        User user = new User();
        user.setPicture(picturePath);

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        String result = userService.getUserPicturePath(userId);

        assertThat(result).isEqualTo(picturePath);
    }

    @Test
    void getUserPicturePath_ShouldReturnNull_WhenUserNotFound() {
        int userId = 1;
        when(repository.findById(userId)).thenReturn(Optional.empty());

        String result = userService.getUserPicturePath(userId);

        assertThat(result).isNull();
    }


}
