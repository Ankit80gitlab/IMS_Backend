package com.cms.core.repository;
import com.cms.core.entity.Device;
import com.cms.core.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> , JpaSpecificationExecutor<Ticket> {
    List<Ticket> findAllByCustomerProductMappingIdIn(List<Integer> customerProductMappingIds);
}
