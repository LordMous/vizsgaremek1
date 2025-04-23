package asz.vizsgaremek;

import asz.vizsgaremek.enums.Role;
import asz.vizsgaremek.model.Announcement;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.AnnouncementRepository;
import asz.vizsgaremek.service.AnnouncementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository repository;

    @InjectMocks
    private AnnouncementService announcementService;


    @Test
    void getAllAnnouncements_ShouldReturnListInDescendingOrder() {
        Announcement a1 = new Announcement("Second", new User());
        Announcement a2 = new Announcement("First", new User());

        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(a1, a2));

        List<Announcement> result = announcementService.getAllAnnouncements();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMessage()).isEqualTo("Second");
        assertThat(result.get(1).getMessage()).isEqualTo("First");
    }

    @Test
    void createAnnouncement_ShouldSave_WhenUserIsAdmin() {
        User admin = new User();
        admin.setRole(Role.ADMIN);

        Announcement saved = new Announcement("New announcement", admin);

        when(repository.save(any(Announcement.class))).thenReturn(saved);

        Announcement result = announcementService.createAnnoucement("New announcement", admin);

        assertThat(result.getMessage()).isEqualTo("New announcement");
        assertThat(result.getSender()).isEqualTo(admin);
    }

    @Test
    void createAnnouncement_ShouldThrowSecurityException_WhenUserIsNotAdmin() {
        User nonAdmin = new User();
        nonAdmin.setRole(Role.USER);

        assertThatThrownBy(() -> announcementService.createAnnoucement("Blocked message", nonAdmin))
                .isInstanceOf(SecurityException.class)
                .hasMessage("You do not have permission to post announcements.");
    }

}
