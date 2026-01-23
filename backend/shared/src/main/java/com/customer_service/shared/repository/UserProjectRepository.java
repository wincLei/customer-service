package com.customer_service.shared.repository;

import com.customer_service.shared.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Long> {

    /**
     * 查找用户关联的所有项目
     */
    @Query("SELECT up FROM UserProject up JOIN FETCH up.project WHERE up.user.id = :userId")
    List<UserProject> findByUserId(@Param("userId") Long userId);

    /**
     * 查找用户关联的项目ID列表
     */
    @Query("SELECT up.project.id FROM UserProject up WHERE up.user.id = :userId")
    List<Long> findProjectIdsByUserId(@Param("userId") Long userId);

    /**
     * 查找关联到某项目的用户ID列表
     */
    @Query("SELECT up.user.id FROM UserProject up WHERE up.project.id = :projectId")
    List<Long> findUserIdsByProjectId(@Param("projectId") Long projectId);

    /**
     * 删除用户的所有项目关联
     */
    @Modifying
    @Query("DELETE FROM UserProject up WHERE up.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否关联了某个项目
     */
    @Query("SELECT COUNT(up) > 0 FROM UserProject up WHERE up.user.id = :userId AND up.project.id = :projectId")
    boolean existsByUserIdAndProjectId(@Param("userId") Long userId, @Param("projectId") Long projectId);
}
