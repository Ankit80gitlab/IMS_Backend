package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.dto.SearchCriteriaTicketDto;
import com.cms.incidentmanagement.dto.TicketDto;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Shashidhar on 5/20/2024.
 */
public interface TicketService {
    HashMap<String, Object> addTicket(TicketDto ticketDto, String token) throws IOException;

    HashMap<String, Object> getAllTickets(SearchCriteriaTicketDto searchCriteriaTicketDto, String token);

    HashMap<String, Object> updateTicket(TicketDto ticketDto, String token)throws IOException;

    HashMap<String, Object> removeTicket(Integer ticketDto);

    HashMap<String, Object> findTicketById(String token, Integer ticketId);

}
