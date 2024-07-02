package com.cms.core.repository;

import com.cms.core.entity.IncidentType;
import com.cms.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by Shashidhar on 6/12/2024.
 */
public interface IncidentTypeRepository extends JpaRepository<IncidentType,Integer>,JpaSpecificationExecutor<IncidentType> {
    List<IncidentType> findByProductId(Integer id);
}
