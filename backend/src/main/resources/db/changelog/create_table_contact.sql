create table contact (
             id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
             user_id INT NOT NULL,
             contact_user_id INT NOT NULL,
             status ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'BLOCKED') DEFAULT 'PENDING',
             FOREIGN KEY (user_id) REFERENCES user(id),
             FOREIGN KEY (contact_user_id) REFERENCES user(id)
);