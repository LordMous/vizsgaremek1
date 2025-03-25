create table chat (
          id INT PRIMARY KEY AUTO_INCREMENT,
          user1_id INT NOT NULL,  -- Az első felhasználó
          user2_id INT NOT NULL,  -- A második felhasználó
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          FOREIGN KEY (user1_id) REFERENCES user(id) ON DELETE CASCADE,
          FOREIGN KEY (user2_id) REFERENCES user(id) ON DELETE CASCADE
);