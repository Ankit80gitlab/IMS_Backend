package com.cms.core.repository;

import com.cms.core.entity.IncidentEscalationDefaultUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Shashidhar on 6/18/2024.
 */
public interface IncidentEscalationDefaultUserRepository extends JpaRepository<IncidentEscalationDefaultUser,Integer> {
    List<IncidentEscalationDefaultUser> findByIncidentEscalationId(Integer id);
}
