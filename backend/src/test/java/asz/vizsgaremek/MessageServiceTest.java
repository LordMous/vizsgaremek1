package asz.vizsgaremek;

import asz.vizsgaremek.dto.message.MessageDTO;
import asz.vizsgaremek.model.Chat;
import asz.vizsgaremek.model.Message;
import asz.vizsgaremek.model.User;
import asz.vizsgaremek.repository.ChatRepository;
import asz.vizsgaremek.repository.MessageRepository;
import asz.vizsgaremek.repository.UserRepository;
import asz.vizsgaremek.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private MessageService messageService;

    // getMessagesForChat – success
    @Test
    void getMessagesForChat_ShouldReturnMessages_WhenUserIsParticipant() {
        int chatId = 1;
        String username = "alice";
        User user = new User();
        user.setUserName(username);

        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setUser1(user);
        chat.setUser2(new User());

        Message message = new Message();
        message.setSender(user);
        message.setMessage("Hello");
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        message.setMessageType("text");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(messageRepository.findByChatOrderByCreatedAtAsc(chat)).thenReturn(List.of(message));

        List<MessageDTO> result = messageService.getMessagesForChat(chatId, username);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo("Hello");
    }

    // getMessagesForChat – user not in chat
    @Test
    void getMessagesForChat_ShouldThrow_WhenUserIsNotParticipant() {
        int chatId = 1;
        String outsiderUsername = "bob";

        User outsider = new User();
        outsider.setUserName(outsiderUsername);

        User user1 = new User();
        user1.setUserName("alice");

        User user2 = new User();
        user2.setUserName("eve");

        Chat chat = new Chat();
        chat.setUser1(user1);
        chat.setUser2(user2);

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findByUserName(outsiderUsername)).thenReturn(Optional.of(outsider));

        assertThatThrownBy(() -> messageService.getMessagesForChat(chatId, outsiderUsername))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User is not part of this chat");
    }

    // sendMessage – success
    @Test
    void sendMessage_ShouldSaveAndReturnMessageDTO_WhenValidInput() {
        int chatId = 1;
        String username = "alice";
        String messageText = "Hi";

        User user = new User();
        user.setUserName(username);

        Chat chat = new Chat();
        chat.setUser1(user);
        chat.setUser2(new User());

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));

        MessageDTO result = messageService.sendMessage(chatId, username, messageText);

        assertThat(result.getSender()).isEqualTo("alice");
        assertThat(result.getMessage()).isEqualTo("Hi");
        assertThat(result.getType()).isEqualTo("text");
    }

    // getChatPartner – returns opposite user
    @Test
    void getChatPartner_ShouldReturnOtherUsername_WhenUserIsUser1() {
        Chat chat = new Chat();
        User user1 = new User(); user1.setUserName("alice");
        User user2 = new User(); user2.setUserName("bob");
        chat.setUser1(user1);
        chat.setUser2(user2);

        when(chatRepository.findById(1)).thenReturn(Optional.of(chat));

        String partner = messageService.getChatPartner(1, "alice");

        assertThat(partner).isEqualTo("bob");
    }

    // saveFileMessage – success
    @Test
    void saveFileMessage_ShouldReturnSavedMessage_WhenDataValid() {
        int senderId = 1, chatId = 2;
        String filePath = "/files/test/file.txt";

        User sender = new User(); sender.setId(senderId);
        Chat chat = new Chat(); chat.setId(chatId);

        when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> i.getArgument(0));

        Message result = messageService.saveFileMessage(senderId, chatId, filePath);

        assertThat(result.getMessage()).isEqualTo(filePath);
        assertThat(result.getMessageType()).isEqualTo("file");
        assertThat(result.getSender()).isEqualTo(sender);
        assertThat(result.getChat()).isEqualTo(chat);
    }
}
