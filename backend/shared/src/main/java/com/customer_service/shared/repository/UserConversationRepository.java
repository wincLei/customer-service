package com.customer_service.shared.repository;

import com.customer_service.shared.entity.UserConversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户会话 Repository
 * 用于客服端工作台的用户列表查询
 */
@Repository
public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {

        /**
         * 根据用户ID查找用户会话
         */
        Optional<UserConversation> findByUserId(Long userId);

        /**
         * 根据项目ID查找所有用户会话，按最后消息时间倒序
         */
        List<UserConversation> findByProjectIdOrderByLastMessageTimeDesc(Long projectId);

        /**
         * 根据项目ID分页查找用户会话，按最后消息时间倒序
         */
        Page<UserConversation> findByProjectIdOrderByLastMessageTimeDesc(Long projectId, Pageable pageable);

        /**
         * 根据项目ID查找有未读消息的用户会话
         */
        List<UserConversation> findByProjectIdAndUnreadCountGreaterThanOrderByLastMessageTimeDesc(
                        Long projectId, Integer unreadCount);

        /**
         * 根据项目ID统计用户会话数量
         */
        long countByProjectId(Long projectId);

        /**
         * 根据项目ID统计有未读消息的用户会话数量
         */
        long countByProjectIdAndUnreadCountGreaterThan(Long projectId, Integer unreadCount);

        /**
         * 查询项目下的用户会话列表，关联用户信息
         */
        @Query("SELECT uc FROM UserConversation uc " +
                        "LEFT JOIN FETCH uc.user u " +
                        "WHERE uc.projectId = :projectId " +
                        "ORDER BY uc.lastMessageTime DESC")
        List<UserConversation> findByProjectIdWithUser(@Param("projectId") Long projectId);

        /**
         * 查询项目下有未读消息的用户会话列表，关联用户信息
         */
        @Query("SELECT uc FROM UserConversation uc " +
                        "LEFT JOIN FETCH uc.user u " +
                        "WHERE uc.projectId = :projectId AND uc.unreadCount > 0 " +
                        "ORDER BY uc.lastMessageTime DESC")
        List<UserConversation> findByProjectIdWithUserAndUnread(@Param("projectId") Long projectId);

        /**
         * 查询多个项目下的用户会话列表，关联用户信息
         * 排序规则：未读消息优先（有未读的在前），然后按最后消息时间倒序
         */
        @Query("SELECT uc FROM UserConversation uc " +
                        "LEFT JOIN FETCH uc.user u " +
                        "WHERE uc.projectId IN :projectIds " +
                        "ORDER BY CASE WHEN uc.unreadCount > 0 THEN 0 ELSE 1 END, uc.lastMessageTime DESC")
        List<UserConversation> findByProjectIdsWithUser(@Param("projectIds") List<Long> projectIds);

        /**
         * 查询多个项目下有未读消息的用户会话列表，关联用户信息
         */
        @Query("SELECT uc FROM UserConversation uc " +
                        "LEFT JOIN FETCH uc.user u " +
                        "WHERE uc.projectId IN :projectIds AND uc.unreadCount > 0 " +
                        "ORDER BY uc.lastMessageTime DESC")
        List<UserConversation> findByProjectIdsWithUserAndUnread(@Param("projectIds") List<Long> projectIds);

        /**
         * 统计多个项目下有未读消息的用户会话数量
         */
        @Query("SELECT COUNT(uc) FROM UserConversation uc WHERE uc.projectId IN :projectIds AND uc.unreadCount > 0")
        long countByProjectIdsAndUnreadCountGreaterThanZero(@Param("projectIds") List<Long> projectIds);

        /**
         * 分页查询多个项目下的用户会话列表，关联用户信息
         * 排序规则：未读消息优先（有未读的在前），然后按最后消息时间倒序
         */
        @Query(value = "SELECT uc FROM UserConversation uc " +
                        "LEFT JOIN FETCH uc.user u " +
                        "WHERE uc.projectId IN :projectIds " +
                        "ORDER BY CASE WHEN uc.unreadCount > 0 THEN 0 ELSE 1 END, uc.lastMessageTime DESC, uc.id DESC", countQuery = "SELECT COUNT(uc) FROM UserConversation uc WHERE uc.projectId IN :projectIds")
        Page<UserConversation> findByProjectIdsWithUserPaged(@Param("projectIds") List<Long> projectIds,
                        Pageable pageable);

        /**
         * 统计多个项目下的用户会话总数
         */
        @Query("SELECT COUNT(uc) FROM UserConversation uc WHERE uc.projectId IN :projectIds")
        long countByProjectIds(@Param("projectIds") List<Long> projectIds);

        /**
         * 分页查询多个项目下的用户会话列表（带搜索条件）
         * 搜索条件：全匹配用户的 uid 或 external_uid
         * 排序规则：未读消息优先（有未读的在前），然后按最后消息时间倒序
         */
        @Query(value = "SELECT uc FROM UserConversation uc " +
                        "LEFT JOIN FETCH uc.user u " +
                        "WHERE uc.projectId IN :projectIds " +
                        "AND (u.uid = :keyword OR u.externalUid = :keyword) " +
                        "ORDER BY CASE WHEN uc.unreadCount > 0 THEN 0 ELSE 1 END, uc.lastMessageTime DESC, uc.id DESC", countQuery = "SELECT COUNT(uc) FROM UserConversation uc "
                                        +
                                        "LEFT JOIN uc.user u " +
                                        "WHERE uc.projectId IN :projectIds " +
                                        "AND (u.uid = :keyword OR u.externalUid = :keyword)")
        Page<UserConversation> findByProjectIdsAndKeywordPaged(
                        @Param("projectIds") List<Long> projectIds,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 统计多个项目下匹配搜索条件的用户会话总数
         */
        @Query("SELECT COUNT(uc) FROM UserConversation uc " +
                        "LEFT JOIN uc.user u " +
                        "WHERE uc.projectId IN :projectIds " +
                        "AND (u.uid = :keyword OR u.externalUid = :keyword)")
        long countByProjectIdsAndKeyword(
                        @Param("projectIds") List<Long> projectIds,
                        @Param("keyword") String keyword);
}
