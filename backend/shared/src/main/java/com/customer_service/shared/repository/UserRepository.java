package com.customer_service.shared.repository;

import com.customer_service.shared.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByProjectIdAndUid(Long projectId, String uid);

        Optional<User> findByProjectIdAndPhone(Long projectId, String phone);

        /**
         * 根据项目ID和外部用户ID查找
         */
        Optional<User> findByProjectIdAndExternalUid(Long projectId, String externalUid);

        /**
         * 根据项目ID分页查询用户
         */
        Page<User> findByProjectId(Long projectId, Pageable pageable);

        /**
         * 根据项目ID和是否游客分页查询
         */
        Page<User> findByProjectIdAndIsGuest(Long projectId, Boolean isGuest, Pageable pageable);

        /**
         * 按项目ID搜索用户（昵称、手机号、UID）
         */
        @Query("SELECT u FROM User u WHERE u.projectId = :projectId AND " +
                        "(u.nickname LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.uid LIKE %:keyword%)")
        Page<User> searchByProjectId(@Param("projectId") Long projectId,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 按多个项目ID分页查询用户
         */
        Page<User> findByProjectIdIn(List<Long> projectIds, Pageable pageable);

        /**
         * 按多个项目ID搜索用户
         */
        @Query("SELECT u FROM User u WHERE u.projectId IN :projectIds AND " +
                        "(u.nickname LIKE %:keyword% OR u.phone LIKE %:keyword% OR u.uid LIKE %:keyword%)")
        Page<User> searchByProjectIdIn(@Param("projectIds") List<Long> projectIds,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        /**
         * 统计项目下的用户数量
         */
        long countByProjectId(Long projectId);

        /**
         * 统计项目下的游客数量
         */
        long countByProjectIdAndIsGuest(Long projectId, Boolean isGuest);

        /**
         * 根据用户ID列表获取用户
         */
        List<User> findByIdIn(List<Long> ids);

        /**
         * 查找已被合并的用户
         */
        List<User> findByMergedFromId(Long mergedFromId);

        /**
         * 根据 UID 查找用户（全局唯一）
         */
        Optional<User> findByUid(String uid);
}
