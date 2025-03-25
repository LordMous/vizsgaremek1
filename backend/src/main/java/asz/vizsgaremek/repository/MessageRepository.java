package asz.vizsgaremek.repository;

import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatOrderByCreatedAtAsc(Chat chat);
}
