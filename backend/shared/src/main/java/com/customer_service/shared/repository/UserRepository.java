package com.customer_service.shared.repository;

import com.customer_service.shared.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProjectIdAndUid(Long projectId, String uid);

    Optional<User> findByProjectIdAndPhone(Long projectId, String phone);
}
