package asz.vizsgaremek.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user1_id")
    private User user1;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user2_id")
    private User user2;
    @JsonIgnore
    @OneToMany(mappedBy = "chat")
    private List<Message> messages;

}
