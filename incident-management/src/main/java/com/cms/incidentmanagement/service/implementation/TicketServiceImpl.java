package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.Ticket;
import com.cms.core.repository.TicketRepository;
import com.cms.incidentmanagement.dto.TicketDto;
import com.cms.incidentmanagement.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Shashidhar on 5/20/2024.
 *
 */
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Override
    public HashMap<String, Object> addTicket(TicketDto ticketDto, String token) {
        HashMap<String,Object> map=new HashMap<>();
        Ticket ticket=new Ticket();
        ticket.setSubject(ticketDto.getSubject());
        ticket.setType(ticketDto.getType());
        ticket.setPriority(ticketDto.getPriority());
        ticket.setIssueRelated(ticketDto.getIssueRelated());
        ticket.setStatus(ticketDto.getStatus());
        ticket.setDescription(ticketDto.getDescription());
        Date time=new Date();






        return null;
    }

    @Override
    public HashMap<String, Object> getAllTickets(Integer pageNo, Integer pageSize) {
        return null;
    }

    @Override
    public HashMap<String, Object> updateTicket(TicketDto ticketDto, String token) {
        return null;
    }

    @Override
    public HashMap<String, Object> removeTicket(Integer ticketDto) {
        return null;
    }
}
