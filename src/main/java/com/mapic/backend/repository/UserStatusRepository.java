package com.mapic.backend.repository;

import com.mapic.backend.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for UserStatus.
 *
 * NOTE: UserStatus uses @MapsId on its @OneToOne(User) relationship,
 * which means the primary key of user_statuses IS the user_id.
 * Therefore findById(userId) is the correct lookup — no custom query needed.
 */
@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    // findById(userId) inherited from JpaRepository — use directly in service/controller
}
