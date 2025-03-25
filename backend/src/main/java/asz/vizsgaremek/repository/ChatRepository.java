package asz.vizsgaremek.repository;

import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ChatRepository extends JpaRepository<Chat,Integer> {
    List<Chat> findByUser1OrUser2(User user1, User user2);

}
