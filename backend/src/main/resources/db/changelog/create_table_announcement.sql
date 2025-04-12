CREATE TABLE announcement (
        id INT AUTO_INCREMENT PRIMARY KEY,
        message TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        sender_id INT,
        FOREIGN KEY (sender_id) REFERENCES user(id)
);