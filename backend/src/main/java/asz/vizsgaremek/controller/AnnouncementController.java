package asz.vizsgaremek.controller;

import asz.vizsgaremek.dto.announcement.AnnouncementRequest;
import asz.vizsgaremek.dto.announcement.AnnouncementResponse;
import asz.vizsgaremek.model.Announcement;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.service.AnnouncementService;
import asz.vizsgaremek.service.UserService;
import asz.vizsgaremek.websocket.WebSocketController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/announcements")
@Tag(name = "Announcement", description = "Here you can get the announcements and even post one if you're an admin")
public class AnnouncementController {

    @Autowired
    private WebSocketController webSocketController;

    private AnnouncementService announcementService;
    private UserService userService;

    @Autowired
    public AnnouncementController(AnnouncementService announcementService, UserService userService) {
        this.announcementService = announcementService;
        this.userService = userService;
    }

    @GetMapping
    public List<AnnouncementResponse> getAnnouncements() {
        return announcementService.getAllAnnouncements().stream()
                .map(a -> new AnnouncementResponse(
                        a.getId(),
                        a.getMessage(),
                        a.getCreatedAt(),
                        a.getSender().getUsername()
                ))
                .toList();
    }

    @PostMapping
    public AnnouncementResponse createAnnouncement(@RequestBody AnnouncementRequest requestDTO) {
        User currentUser = userService.getCurrentAuthenticatedUser(); // pl. SecurityContext alapján
        Announcement saved = announcementService.createAnnoucement(requestDTO.getMessage(), currentUser);

        AnnouncementResponse response =  new AnnouncementResponse(
                saved.getId(),
                saved.getMessage(),
                saved.getCreatedAt(),
                saved.getSender().getUsername()
        );
        webSocketController.broadcastAnnouncement(response);
        return response;
    }
}
