package com.cms.incidentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TicketDto {

    private Integer id;

    private String subject;

    private Integer typeId;

    private String issueRelated;

    private String priority;

    private String status;

    private String description;

    private Integer assignedTo;

    private Integer customerId;

    private Integer productId;

    private Integer deviceId;//optional

    private List<TicketFileDto> ticketFileDtos;

    private Integer updatedBy;//optional

    private String comment;

    private String commentCreatedBy;

    private Integer parentTicketId;

    private Boolean isEditable;

    private Integer originalTicketId;
}
