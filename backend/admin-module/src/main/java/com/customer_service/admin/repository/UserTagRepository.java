package com.customer_service.admin.repository;

import com.customer_service.shared.entity.UserTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTagRepository extends JpaRepository<UserTag, Long> {

    List<UserTag> findByUserId(Long userId);

    List<UserTag> findByProjectIdAndTagName(Long projectId, String tagName);

    void deleteByUserIdAndTagName(Long userId, String tagName);
}
