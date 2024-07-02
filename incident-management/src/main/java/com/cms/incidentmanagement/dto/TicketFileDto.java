package com.cms.incidentmanagement.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by Shashidhar on 6/26/2024.
 */
@Getter
@Setter
public class TicketFileDto {

    private MultipartFile file;

    private String optionalDescription;
}
