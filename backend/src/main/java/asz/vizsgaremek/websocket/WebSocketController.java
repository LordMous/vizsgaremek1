package asz.vizsgaremek.websocket;

import asz.vizsgaremek.dto.announcement.AnnouncementResponse;
import asz.vizsgaremek.dto.contact.ContactDTO;
import asz.vizsgaremek.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate template;

    private MessageService messageService;

    @MessageMapping("/chat")
    @SendTo("/user/queue/messages") // Ezzel jelezheted, hogy a privát csatornára küldöd
    public String sendMessage(String message) {
        //messageService.sendMessage(message.getChatId(), message.getSenderUsername(), message.getContent());

        return message; // Visszaküldjük az üzenetet a klienseknek
    }

    public void sendContactUpdate(ContactDTO dto) {
        System.out.println("ADATKULDES CONTACT");
        template.convertAndSend("/topic/contacts",dto);
        //template.convertAndSend(username, "/user/topic/contacts", message);
    }

    public void broadcastAnnouncement(AnnouncementResponse response) {
        template.convertAndSend("/topic/announcements", response);
    }

}
