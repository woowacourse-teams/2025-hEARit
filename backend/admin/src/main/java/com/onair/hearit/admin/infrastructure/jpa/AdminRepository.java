package com.onair.hearit.admin.infrastructure.jpa;


import com.onair.hearit.admin.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);
}
