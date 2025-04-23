package asz.vizsgaremek;

import asz.vizsgaremek.dto.contact.ContactDTO;
import asz.vizsgaremek.enums.Status;
import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ContactRepository;
import asz.vizsgaremek.repository.UserRepository;
import asz.vizsgaremek.service.ContactService;
import asz.vizsgaremek.websocket.WebSocketController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WebSocketController webSocketController;

    @InjectMocks
    private ContactService contactService;

    // addContact
    @Test
    void addContact_ShouldSaveContactWithPendingStatus() {
        User user = new User(); user.setUserName("alice");
        User contactUser = new User(); contactUser.setUserName("bob");

        Contact savedContact = new Contact();
        savedContact.setUser(user);
        savedContact.setContactUser(contactUser);
        savedContact.setStatus(Status.PENDING);

        when(contactRepository.save(any(Contact.class))).thenReturn(savedContact);

        Contact result = contactService.addContact(user, contactUser);

        assertThat(result.getStatus()).isEqualTo(Status.PENDING);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getContactUser()).isEqualTo(contactUser);

        verify(webSocketController).sendContactUpdate(any(ContactDTO.class));
    }

    // getContactsByStatus
    @Test
    void getContactsByStatus_ShouldReturnMergedList() {
        User user = new User(); user.setId(1);

        Contact c1 = new Contact(); c1.setUser(user);
        Contact c2 = new Contact(); c2.setContactUser(user);

        when(contactRepository.findByUserAndStatus(user, Status.PENDING)).thenReturn(new ArrayList<>(List.of(c1)));
        when(contactRepository.findByContactUserAndStatus(user, Status.PENDING)).thenReturn(new ArrayList<>(List.of(c2)));

        List<Contact> result = contactService.getContactsByStatus(user, Status.PENDING);

        assertThat(result).containsExactlyInAnyOrder(c1, c2);
    }

    // updateContactStatus
    @Test
    void updateContactStatus_ShouldUpdateAndReturnContact_WhenValid() {
        int userId = 1;
        int contactUserId = 2;


        User contactUser = new User(); contactUser.setId(userId); // ez a feltétel
        User requester = new User(); requester.setId(contactUserId);
        Contact contact = new Contact();
        contact.setContactUser(contactUser);
        contact.setUser(requester);
        contact.setStatus(Status.PENDING);


        when(contactRepository.findByUserIdAndContactUserId(contactUserId, userId))
                .thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(i -> i.getArgument(0));

        Contact updated = contactService.updateContactStatus(userId, contactUserId, Status.ACCEPTED);

        assertThat(updated.getStatus()).isEqualTo(Status.ACCEPTED);
        verify(webSocketController).sendContactUpdate(any(ContactDTO.class));
    }

    @Test
    void updateContactStatus_ShouldThrow_WhenUserIsNotRecipient() {
        int userId = 1;
        int contactUserId = 2;

        User requester = new User(); requester.setId(contactUserId);
        User recipient = new User(); recipient.setId(999); // not matching

        Contact contact = new Contact();
        contact.setContactUser(recipient); // wrong user!
        contact.setUser(requester);
        contact.setStatus(Status.PENDING);

        when(contactRepository.findByUserIdAndContactUserId(contactUserId, userId))
                .thenReturn(Optional.of(contact));

        assertThatThrownBy(() -> contactService.updateContactStatus(userId, contactUserId, Status.ACCEPTED))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Permission denied: Only the recipient can update the contact status.");
    }

    // deleteContact
    @Test
    void deleteContact_ShouldDeleteContact_WhenExists() {
        int userId = 1;
        int contactUserId = 2;

        Contact contact = new Contact();
        contact.setUser(new User());
        contact.setContactUser(new User());
        contact.setStatus(Status.PENDING);

        when(contactRepository.findByUserIdAndContactUserId(userId, contactUserId))
                .thenReturn(Optional.of(contact));

        contactService.deleteContact(userId, contactUserId);

        verify(webSocketController).sendContactUpdate(any(ContactDTO.class));
        verify(contactRepository).delete(contact);
    }

    @Test
    void deleteContact_ShouldThrow_WhenContactNotFound() {
        int userId = 1, contactUserId = 2;

        when(contactRepository.findByUserIdAndContactUserId(userId, contactUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.deleteContact(userId, contactUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Contact not found between user 1 and 2");
    }
}
