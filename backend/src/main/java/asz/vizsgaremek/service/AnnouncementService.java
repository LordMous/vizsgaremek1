package asz.vizsgaremek.service;

import asz.vizsgaremek.enums.Role;
import asz.vizsgaremek.model.Announcement;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {
    private AnnouncementRepository repository;

    @Autowired
    public AnnouncementService(AnnouncementRepository repository) {
        this.repository = repository;
    }

    public List<Announcement> getAllAnnouncements() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Announcement createAnnoucement(String message, User sender) {
        Role role = Role.ADMIN;
        if (!role.equals(sender.getRole())) {
            throw new SecurityException("You do not have permission to post announcements.");
        }

        Announcement announcement = new Announcement(message,sender);
        return repository.save(announcement);
    }
}
