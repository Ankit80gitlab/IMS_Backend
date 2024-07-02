package com.cms.core.repository;

import com.cms.core.entity.IncidentEscalationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by Shashidhar on 6/14/2024.
 */
public interface IncidentEscalationUserRepository extends JpaRepository<IncidentEscalationUser,Integer> {
    List<IncidentEscalationUser> findByIncidentEscalationId(Integer id);
}
