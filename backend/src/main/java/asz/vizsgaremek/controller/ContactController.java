package asz.vizsgaremek.controller;

import asz.vizsgaremek.dto.user.UserRead;
import asz.vizsgaremek.dto.contact.ContactDTO;
import asz.vizsgaremek.enums.Status;
import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ContactRepository;
import asz.vizsgaremek.service.ContactService;
import asz.vizsgaremek.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contacts")
@Tag(name = "Contact functions", description = "Here you can manage the user contacts")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @Autowired
    private ContactRepository contactRepository;

    @PostMapping("/add")
    @Operation(summary = "Add a new user")
    public ResponseEntity<ContactDTO> addContact(@RequestParam Integer userId, @RequestParam Integer contactUserId) {
        User user = userService.readUserEntity(userId);
        User contactUser = userService.readUserEntity(contactUserId);
        Contact contact = contactService.addContact(user, contactUser);
        return ResponseEntity.ok(new ContactDTO(contact));
    }

    // Kontaktok lekérdezése státusz alapján
    @GetMapping("/{userId}")
    @Operation(summary = "Get all the contacts based on the status of the contact")
    public ResponseEntity<List<ContactDTO>> getContactsByStatus(@PathVariable Integer userId, @RequestParam Status status) {
        User user = userService.readUserEntity(userId); // User entitás lekérése
        List<Contact> contacts = contactService.getContactsByStatus(user, status); // Kontaktok lekérdezése

        // Contact entitások átalakítása ContactDTO-vá
        List<ContactDTO> contactDTOs = contacts.stream()
                .map(ContactDTO::new) // Minden Contact entitást DTO-vá alakítunk
                .collect(Collectors.toList());

        return ResponseEntity.ok(contactDTOs);
    }

    // Kontakt státuszának frissítése
    @PutMapping("/{userId}")
    @Operation(summary = "Change the status of the contact")
    public ResponseEntity<ContactDTO> updateContactStatus(
            @PathVariable Integer userId,
            @RequestParam Integer contactUserId,
            @RequestParam Status status) {

        Contact updatedContact = contactService.updateContactStatus(userId, contactUserId, status);
        return ResponseEntity.ok(new ContactDTO(updatedContact));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a contact")
    public ResponseEntity<ContactDTO> deleteContact(
            @PathVariable Integer userId,
            @RequestParam Integer contactUserId) {
        contactService.deleteContact(userId, contactUserId);
        return ResponseEntity.noContent().build();
    }



}
