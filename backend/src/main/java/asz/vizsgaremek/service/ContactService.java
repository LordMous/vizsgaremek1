package asz.vizsgaremek.service;

import asz.vizsgaremek.dto.contact.ContactDTO;
import asz.vizsgaremek.enums.Status;
import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ContactRepository;
import asz.vizsgaremek.repository.UserRepository;
import asz.vizsgaremek.websocket.WebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketController webSocketController;


    public Contact addContact(User user, User contactUser) {
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setContactUser(contactUser);
        contact.setStatus(Status.PENDING);

        ContactDTO dto = new ContactDTO(contact);
        webSocketController.sendContactUpdate(dto);
        System.out.println(contactUser.getUsername()+" "+user.getUsername());
        return contactRepository.save(contact);
    }

    public List<Contact> getContactsByStatus(User user, Status status) {
        List<Contact> sentRequests = contactRepository.findByUserAndStatus(user, status);
        List<Contact> receivedRequests = contactRepository.findByContactUserAndStatus(user, status);

        // Összefűzzük a két listát
        sentRequests.addAll(receivedRequests);
        return sentRequests;
    }

    public Contact updateContactStatus(Integer userId, Integer contactUserId, Status status) {
        Contact contact = contactRepository.findByUserIdAndContactUserId(contactUserId, userId)
                .orElseThrow(() -> new RuntimeException("Contact not found between user " + userId + " and " + contactUserId));

        // Ellenőrizzük, hogy a kérő fél a contactUser-e
        if (!contact.getContactUser().getId().equals(userId)) {
            throw new RuntimeException("Permission denied: Only the recipient can update the contact status.");
        }

        contact.setStatus(status);
        webSocketController.sendContactUpdate(new ContactDTO(contact));

        return contactRepository.save(contact);
    }

    public void deleteContact(Integer userId, Integer contactUserId) {
        Contact contact = contactRepository.findByUserIdAndContactUserId(userId,contactUserId)
                .orElseThrow(() -> new RuntimeException("Contact not found between user " + userId + " and " + contactUserId));
        webSocketController.sendContactUpdate(new ContactDTO(contact));

        contactRepository.delete(contact);
    }


}
