package at.salzburg.hackathon.hackathontourism03.persistence;

import at.salzburg.hackathon.hackathontourism03.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findUserById(Long id);
}
