create table message (
             id INT PRIMARY KEY AUTO_INCREMENT,
             chat_id INT NOT NULL,  -- A csevegés, amelyhez az üzenet tartozik
             sender_id INT NOT NULL,  -- Az üzenet küldője
             message TEXT NOT NULL,  -- Az üzenet szövege
             message_type ENUM('text', 'image', 'video', 'file') DEFAULT 'text',  -- Az üzenet típusa
             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
             FOREIGN KEY (chat_id) REFERENCES chat(id) ON DELETE CASCADE,
             FOREIGN KEY (sender_id) REFERENCES user(id) ON DELETE CASCADE
);