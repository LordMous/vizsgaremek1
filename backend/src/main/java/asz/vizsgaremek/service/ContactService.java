package asz.vizsgaremek.service;

import asz.vizsgaremek.enums.Status;
import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;


    public Contact addContact(User user, User contactUser, Status status) {
        Contact contact = new Contact();
        return contactRepository.save(contact);
    }
}
