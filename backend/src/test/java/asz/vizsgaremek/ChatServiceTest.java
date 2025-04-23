package asz.vizsgaremek;

import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ChatRepository;
import asz.vizsgaremek.repository.UserRepository;
import asz.vizsgaremek.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatService chatService;

    // getChatsForUser
    @Test
    void getChatsForUser_ShouldReturnChats_WhenUserExists() {
        String username = "alice";
        User user = new User();
        user.setUserName(username);

        Chat chat = new Chat();
        chat.setUser1(user);

        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(chatRepository.findByUser1OrUser2(user, user)).thenReturn(List.of(chat));

        List<Chat> result = chatService.getChatsForUser(username);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser1()).isEqualTo(user);
    }

    // getChatsForUser – user not found
    @Test
    void getChatsForUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatsForUser("ghost"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // getChatById
    @Test
    void getChatById_ShouldReturnChat_WhenExists() {
        Chat chat = new Chat();
        chat.setId(1);

        when(chatRepository.findById(1)).thenReturn(Optional.of(chat));

        Chat result = chatService.getChatById(1);

        assertThat(result.getId()).isEqualTo(1);
    }

    @Test
    void getChatById_ShouldThrow_WhenNotFound() {
        when(chatRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatById(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chat not found");
    }

    // startChat – new chat
    @Test
    void startChat_ShouldCreateNewChat_WhenNoExistingChat() {
        User user1 = new User(); user1.setId(1);
        User user2 = new User(); user2.setId(2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        when(chatRepository.findByUser1OrUser2(user1, user2)).thenReturn(List.of());

        Chat newChat = new Chat();
        newChat.setUser1(user1);
        newChat.setUser2(user2);

        when(chatRepository.save(any(Chat.class))).thenReturn(newChat);

        Chat result = chatService.startChat(1, 2);

        assertThat(result.getUser1()).isEqualTo(user1);
        assertThat(result.getUser2()).isEqualTo(user2);
    }

    // startChat – existing chat
    @Test
    void startChat_ShouldReturnExistingChat_WhenAlreadyExists() {
        User user1 = new User(); user1.setId(1);
        User user2 = new User(); user2.setId(2);

        Chat existing = new Chat();
        existing.setUser1(user1);
        existing.setUser2(user2);

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));
        when(chatRepository.findByUser1OrUser2(user1, user2)).thenReturn(List.of(existing));

        Chat result = chatService.startChat(1, 2);

        assertThat(result).isEqualTo(existing);
    }

    // deleteChat
    @Test
    void deleteChat_ShouldNotifyAndDelete_WhenChatExists() {
        Chat chat = new Chat();
        chat.setId(1);

        User user1 = new User(); user1.setUserName("alice");
        User user2 = new User(); user2.setUserName("bob");

        chat.setUser1(user1);
        chat.setUser2(user2);

        when(chatRepository.findById(1)).thenReturn(Optional.of(chat));

        chatService.deleteChat(1);

        verify(messagingTemplate).convertAndSendToUser("alice", "/queue/chat-deleted", 1);
        verify(messagingTemplate).convertAndSendToUser("bob", "/queue/chat-deleted", 1);
        verify(chatRepository).delete(chat);
    }

    @Test
    void deleteChat_ShouldThrow_WhenChatNotFound() {
        when(chatRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.deleteChat(1))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Chat not found with ID: 1");
    }
}
