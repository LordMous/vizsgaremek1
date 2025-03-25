package asz.vizsgaremek.repository;

import asz.vizsgaremek.model.Contact;
import asz.vizsgaremek.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {


}
