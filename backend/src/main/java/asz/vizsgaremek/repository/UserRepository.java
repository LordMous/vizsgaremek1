package asz.vizsgaremek.repository;

import asz.vizsgaremek.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
    value = "UPDATE user u SET u.picture = :picture WHERE u.id = :id")
    void updateUserPic(@Param("id") Integer id, @Param("picture") String picture);

}


