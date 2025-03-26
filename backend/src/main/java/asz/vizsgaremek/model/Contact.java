package asz.vizsgaremek.model;

import asz.vizsgaremek.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "contact_user_id", referencedColumnName = "id")
    private User contactUser;

    @Enumerated(EnumType.STRING)
    private Status status;

    // További mezők, ha szükséges
    // Getterek és setterek
}
