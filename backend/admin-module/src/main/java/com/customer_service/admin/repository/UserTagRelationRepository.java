package com.customer_service.admin.repository;

import com.customer_service.shared.entity.UserTagRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户标签关联 Repository
 */
@Repository
public interface UserTagRelationRepository extends JpaRepository<UserTagRelation, Long> {

    /**
     * 根据用户ID获取所有标签关联
     */
    List<UserTagRelation> findByUserId(Long userId);

    /**
     * 根据标签ID获取所有用户关联
     */
    List<UserTagRelation> findByTagId(Long tagId);

    /**
     * 检查用户是否已有某标签
     */
    boolean existsByUserIdAndTagId(Long userId, Long tagId);

    /**
     * 查找特定用户和标签的关联
     */
    Optional<UserTagRelation> findByUserIdAndTagId(Long userId, Long tagId);

    /**
     * 删除用户的某个标签
     */
    @Modifying
    void deleteByUserIdAndTagId(Long userId, Long tagId);

    /**
     * 删除用户的所有标签
     */
    @Modifying
    void deleteByUserId(Long userId);

    /**
     * 获取用户的所有标签ID
     */
    @Query("SELECT r.tagId FROM UserTagRelation r WHERE r.userId = :userId")
    List<Long> findTagIdsByUserId(@Param("userId") Long userId);

    /**
     * 获取标签关联的所有用户ID
     */
    @Query("SELECT r.userId FROM UserTagRelation r WHERE r.tagId = :tagId")
    List<Long> findUserIdsByTagId(@Param("tagId") Long tagId);

    /**
     * 批量检查用户是否有某些标签
     */
    @Query("SELECT r.userId FROM UserTagRelation r WHERE r.userId IN :userIds AND r.tagId = :tagId")
    List<Long> findUserIdsWithTag(@Param("userIds") List<Long> userIds, @Param("tagId") Long tagId);
}
