package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.dto.TicketDto;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/20/2024.
 */
public interface TicketService {
    HashMap<String, Object> addTicket(TicketDto ticketDto, String token);

    HashMap<String, Object> getAllTickets(Integer pageNo, Integer pageSize);

    HashMap<String, Object> updateTicket(TicketDto ticketDto, String token);

    HashMap<String, Object> removeTicket(Integer ticketDto);





}
