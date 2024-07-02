package com.cms.core.repository;

import com.cms.core.entity.IncidentEscalation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Shashidhar on 6/14/2024.
 */
public interface IncidentEscalationRepository extends JpaRepository<IncidentEscalation,Integer> {
    Optional<IncidentEscalation> findByIncidentTypeIdAndCustomerProductMappingId(Integer id, Integer id1);

    List<IncidentEscalation> findByCustomerProductMappingId(Integer id);
}
