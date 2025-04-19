package asz.vizsgaremek.dto.contact;

import asz.vizsgaremek.model.Contact;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactDTO {
    private Integer id;
    private Integer userId; // A kapcsolathoz tartozó felhasználó ID-ja
    private Integer contactUserId; // A kontakt felhasználó ID-ja
    private String contactUserName; // A kontakt felhasználó neve
    private String contactEmail; // A kontakt felhasználó email címe
    private String status; // A kapcsolat státusza (pl. "PENDING", "ACCEPTED")
    private String userName;

    // Konstruktor az entitásból
    public ContactDTO(Contact contact) {
        this.id = contact.getId();
        this.userId = contact.getUser().getId();
        this.contactUserId = contact.getContactUser().getId();
        this.contactUserName = contact.getContactUser().getUserName();
        this.contactEmail = contact.getContactUser().getEmail();
        this.status = contact.getStatus().name(); // Enum konvertálása String-re
        this.userName = contact.getUser().getUserName();
    }
}
