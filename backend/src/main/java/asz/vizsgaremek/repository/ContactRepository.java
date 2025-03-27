package asz.vizsgaremek.repository;

import asz.vizsgaremek.enums.Status;
import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByUserAndStatus(User user, Status status);
    Optional<Contact> findByUserIdAndContactUserId(Integer userId, Integer contactUserId);
    List<Contact> findByContactUserAndStatus(User contactUser, Status status);
}
