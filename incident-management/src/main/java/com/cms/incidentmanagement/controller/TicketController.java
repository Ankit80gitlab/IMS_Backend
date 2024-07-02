package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.SearchCriteriaTicketDto;
import com.cms.incidentmanagement.dto.TicketDto;
import com.cms.incidentmanagement.service.implementation.TicketServiceImpl;
import com.cms.incidentmanagement.utility.Constant;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/20/2024.
 */
@SecurityRequirement(name = Constant.BEARER_AUTH)
@RestController
@RequestMapping("/ticketManagement")
public class TicketController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    @Autowired
    private ExceptionConfig exceptionConfig;
    @Autowired
    private TicketServiceImpl ticketServiceImpl;

    @PostMapping("/addTicket")
    public HashMap<String, Object> addTicket(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @ModelAttribute TicketDto ticketDto) {
        HashMap<String, Object> map;
        try {

            map = ticketServiceImpl.addTicket(ticketDto, token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllTickets")
    public HashMap<String, Object> getAllTickets(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @ModelAttribute SearchCriteriaTicketDto searchCriteriaTicketDto

    ) {
        HashMap<String, Object> map;
        try {
            map = ticketServiceImpl.getAllTickets(searchCriteriaTicketDto, token);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @PutMapping("/updateTicket")
    public HashMap<String, Object> updateTicket(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @ModelAttribute TicketDto ticketDto) {
        HashMap<String, Object> map;
        try {
            map = ticketServiceImpl.updateTicket(ticketDto, token);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @DeleteMapping("/deleteTicket")
    public HashMap<String, Object> removeTicket(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "ticketId", required = false) Integer ticketId) {
        HashMap<String, Object> map;
        try {
            map = ticketServiceImpl.removeTicket(ticketId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/findTicketById")
    public HashMap<String, Object> findTicketById(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "ticketId") Integer ticketId
    ) {
        HashMap<String, Object> map;
        try {
            map = ticketServiceImpl.findTicketById(token, ticketId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }
}
