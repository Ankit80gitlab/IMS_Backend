package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.dto.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    // mapped as /app/application
    @MessageMapping("/application")
    @SendTo("/all/messages")
    public Message send(final Message message) {
        return message;
    }

    // mapped as /app/private
    @MessageMapping("/private")
    public void sendToSpecificUser(@Payload Message message) {
        System.out.println("private");
    }
}
