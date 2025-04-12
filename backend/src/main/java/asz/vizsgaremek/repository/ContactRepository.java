package asz.vizsgaremek.repository;

import asz.vizsgaremek.enums.Status;
import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByUserAndStatus(User user, Status status);
    Optional<Contact> findByUserIdAndContactUserId(Integer userId, Integer contactUserId);
    List<Contact> findByContactUserAndStatus(User contactUser, Status status);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
    value = "DELETE FROM contact WHERE contact_user_id = :user_id OR user_id = :user_id")
    void deleteContactByUserId(@Param("user_id") Integer user_id);
}
