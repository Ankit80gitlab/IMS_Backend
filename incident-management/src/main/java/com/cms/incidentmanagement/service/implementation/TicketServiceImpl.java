package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.SearchCriteriaTicketDto;
import com.cms.incidentmanagement.dto.TicketDto;
import com.cms.incidentmanagement.dto.TicketFileDto;
import com.cms.incidentmanagement.service.TicketService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Shashidhar on 5/20/2024.
 */
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private Utilities utility;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerProductMappingRepository customerProductMappingRepository;
    @Autowired
    private CustomerProductMappingDeviceRepository customerProductMappingDeviceRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TicketFileRepository ticketFileRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TicketUpdationHistoryRepository ticketUpdationHistoryRepository;
    @Autowired
    private IncidentTypeRepository incidentTypeRepository;
    @Value("${ticket.file.path}")
    private String fileFolderPath;

    @Override
    public HashMap<String, Object> addTicket(TicketDto ticketDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Ticket ticket = new Ticket();
        ticket.setSubject(ticketDto.getSubject());
        if (ticketDto.getParentTicketId() != null) {
            Optional<Ticket> ticketOptional = ticketRepository.findById(ticketDto.getParentTicketId());
            if (ticketOptional.isPresent()) {
                Ticket ticket1 = ticketOptional.get();
                ticket.setParentTicket(ticket1);
            }
        }
        Optional<IncidentType> incidentTypeOptional = incidentTypeRepository.findById(ticketDto.getTypeId());
        if (!incidentTypeOptional.isPresent()) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.INCIDENT_TYPE_NOT_FOUND);
            return map;
        } else {
            IncidentType incidentType = incidentTypeOptional.get();
            ticket.setIncidentType(incidentType);
        }
        ticket.setPriority(ticketDto.getPriority());
        ticket.setIssueRelated(ticketDto.getIssueRelated());
        ticket.setStatus(ticketDto.getStatus());
        ticket.setDescription(ticketDto.getDescription());
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        ticket.setCreatedTime(timestamp);
        User user = utility.getLoggedInUser(token);
        ticket.setCreatedUser(user);
        if (ticketDto.getAssignedTo() != null) {
            Optional<User> optionalUser = userRepository.findById(ticketDto.getAssignedTo());
            if (optionalUser.isPresent()) {
                User user1 = optionalUser.get();
                ticket.setAssignedTo(user1);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
                return map;
            }
        } else {
            ticket.setAssignedTo(null);
        }

        if (ticketDto.getUpdatedBy() != null) {
            Optional<User> optionalUsr = userRepository.findById(ticketDto.getUpdatedBy());
            if (optionalUsr.isPresent()) {
                User user1 = optionalUsr.get();
                ticket.setUpdatedBy(user1);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
                return map;
            }
        } else {
            ticket.setUpdatedBy(null);
        }
        Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findByCustomerIdAndProductId(ticketDto.getCustomerId(), ticketDto.getProductId());
        if (customerProductMappingOptional.isPresent()) {
            CustomerProductMapping customerProductMapping = customerProductMappingOptional.get();
            ticket.setCustomerProductMapping(customerProductMapping);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "The customer does not have any products assigned to their account .Please ensure the customer has an associated product before creating a ticket");
            return map;
        }
        Optional<CustomerProductMappingDevice> customerProductMappingDeviceOptional = customerProductMappingDeviceRepository.findByCustomerProductMappingIdAndDeviceId(customerProductMappingOptional.get().getId(), ticketDto.getDeviceId());
        if (customerProductMappingDeviceOptional.isPresent()) {
            CustomerProductMappingDevice customerProductMappingDevice = customerProductMappingDeviceOptional.get();
            ticket.setCustomerProductMappingDevice(customerProductMappingDevice);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "The customer does not have any device for the product assigned to their account .Please ensure that for a perticular product,customer has an associated device before creating a ticket");
            return map;
        }
        Ticket savedTicket = ticketRepository.save(ticket);
        Comment comment = new Comment();
        if (ticketDto.getComment() != null) {
            comment.setComment(ticketDto.getComment());
            comment.setUser(user);
            Date date1 = new Date();
            Timestamp timestamp1 = new Timestamp(date1.getTime());
            comment.setCreatedTime(timestamp1);
            comment.setTicket(ticket);
            commentRepository.save(comment);
        }

        if (ticketDto.getTicketFileDtos() != null && !ticketDto.getTicketFileDtos().isEmpty()) {
            for (TicketFileDto ticketFileDto : ticketDto.getTicketFileDtos()) {
                TicketFile ticketFile = new TicketFile();
                ticketFile.setOptionalDescription(ticketFileDto.getOptionalDescription());
                ticketFile.setTicket(ticket);
                MultipartFile multipartFile = ticketFileDto.getFile();
                if (multipartFile != null && !multipartFile.isEmpty()) {
                    try {
                        File baseFolder = new File(fileFolderPath);
                        if (!baseFolder.exists()) {
                            baseFolder.mkdirs();
                        }
                        String dateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        String ticketFolderName = String.valueOf(ticket.getId());
                        String directoryPath = baseFolder + File.separator + dateFolder + File.separator + ticketFolderName;
                        String originalFileName = multipartFile.getOriginalFilename();
                        String extension = getFileExtensionFromMimeType(multipartFile.getContentType());
                        if ("unknown".equals(extension)) {
                            int dotIndex = originalFileName.lastIndexOf('.');
                            if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                                extension = originalFileName.substring(dotIndex + 1).toLowerCase();
                            }
                        }
                        ticketFile.setFileType(extension);
                        String fileNameWithoutExtension;
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                            fileNameWithoutExtension = originalFileName.substring(0, dotIndex);
                        } else {
                            fileNameWithoutExtension = originalFileName;
                        }

                        String filePath = directoryPath + File.separator + fileNameWithoutExtension;
                        String filePathToSave = File.separator + dateFolder + File.separator + ticketFolderName;
                        File directory = new File(directoryPath);
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        File file = new File(filePath);
                        file.getParentFile().mkdirs();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            file.createNewFile();
                            fileOutputStream.write(multipartFile.getBytes());
                            fileOutputStream.close();
                        }
                        ticketFile.setFilePath(filePathToSave);
                        ticketFile.setFileType(extension);
                        ticketFile.setFileName(fileNameWithoutExtension);
                        if (comment != null) {
                            ticketFile.setComment(comment);
                        } else {
                            ticketFile.setComment(null);
                        }
                    } catch (IOException e) {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, "Failed to save the file.");
                        return map;
                    }
                    ticketFileRepository.save(ticketFile);
                }
            }
        }

        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.CREATE_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedTicket.getId());
        }});
        return map;
    }


    @Override
    public HashMap<String, Object> getAllTickets(SearchCriteriaTicketDto searchCriteriaTicketDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> ticketList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        Sort sort = Sort.by(searchCriteriaTicketDto.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.Order.desc("createdTime")
                : Sort.Order.asc("createdTime"));
        PageRequest pageRequest = PageRequest.of(searchCriteriaTicketDto.getPageNo(), searchCriteriaTicketDto.getPageSize(), sort);
        Page<Ticket> ticketPage = ticketRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                Join<Ticket, User> userJoin = root.join("assignedTo", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(userJoin.get("customer"), userCustomer));
            }
            if (searchCriteriaTicketDto.getType() != null && !searchCriteriaTicketDto.getType().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), "%" + searchCriteriaTicketDto.getType().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getSubject() != null && !searchCriteriaTicketDto.getSubject().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("subject")), "%" + searchCriteriaTicketDto.getSubject().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getIssueRelated() != null && !searchCriteriaTicketDto.getIssueRelated().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("issueRelated")), "%" + searchCriteriaTicketDto.getIssueRelated().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getStatus() != null && !searchCriteriaTicketDto.getStatus().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("status")), "%" + searchCriteriaTicketDto.getStatus().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getAssignedTo() != null && !searchCriteriaTicketDto.getAssignedTo().isEmpty()) {
                Join<Ticket, User> assignedToJoin = root.join("assignedTo", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(assignedToJoin.get("username")), "%" + searchCriteriaTicketDto.getAssignedTo().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getCustomerName() != null && !searchCriteriaTicketDto.getCustomerName().isEmpty()) {
                Join<Ticket, CustomerProductMapping> customerProductMappingJoin = root.join("customerProductMapping", JoinType.LEFT);
                Join<CustomerProductMapping, Customer> customerJoin = customerProductMappingJoin.join("customer", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + searchCriteriaTicketDto.getCustomerName().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getProductName() != null && !searchCriteriaTicketDto.getProductName().isEmpty()) {
                Join<Ticket, CustomerProductMapping> customerProductMappingsJoin = root.join("customerProductMapping", JoinType.LEFT);
                Join<CustomerProductMapping, Product> productJoin = customerProductMappingsJoin.join("product", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(productJoin.get("name")), "%" + searchCriteriaTicketDto.getProductName().toLowerCase() + "%"));
            }
            if (searchCriteriaTicketDto.getDeviceName() != null && !searchCriteriaTicketDto.getDeviceName().isEmpty()) {
                Join<Ticket, CustomerProductMappingDevice> ticketCustomerProductMappingDeviceJoin = root.join("customerProductMappingDevice", JoinType.LEFT);
                Join<CustomerProductMappingDevice, Device> customerProductMappingDeviceDeviceJoin = ticketCustomerProductMappingDeviceJoin.join("device", JoinType.LEFT);
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(customerProductMappingDeviceDeviceJoin.get("name")), "%" + searchCriteriaTicketDto.getDeviceName().toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        ticketPage.stream().forEach(ticket -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", ticket.getId());
            data.put("subject", ticket.getSubject());
            Integer incidentTypeId = ticket.getIncidentType().getId();
            Optional<IncidentType> incidentTypeOptional = incidentTypeRepository.findById(incidentTypeId);
            if (incidentTypeOptional.isPresent()) {
                IncidentType incidentType = incidentTypeOptional.get();
                data.put("type", incidentType.getType());
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.INCIDENT_TYPE_NOT_FOUND);

            }
            data.put("issueRelated", ticket.getIssueRelated());
            data.put("priority", ticket.getPriority());
            data.put("status", ticket.getStatus());
            Map<String, Object> userDetails = new HashMap<>();
            Optional<User> userOptional = userRepository.findById(ticket.getAssignedTo().getId());
            if (userOptional.isPresent()) {
                User user1 = userOptional.get();
                userDetails.put("id", user1.getId());
                userDetails.put("userName", user1.getUsername());
            }
            data.put("assignedTo", userDetails);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, ticketList);
        return map;
    }

    @Override
    public HashMap<String, Object> updateTicket(TicketDto ticketDto, String token) throws IOException {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketDto.getId());
        if (!ticketOptional.isPresent()) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "Ticket not found");
            return map;
        }
        Ticket ticket = ticketOptional.get();
        if (ticketDto.getOriginalTicketId() != null) {
            Timestamp date = ticket.getCreatedTime();
            Optional<Ticket> ticketOptional1 = ticketRepository.findById(ticketDto.getOriginalTicketId());
            if (ticketOptional1.isPresent()) {
                Ticket ticket1 = ticketOptional1.get();
                Timestamp date1 = ticket1.getCreatedTime();
                if (date1.before(date)) {
                    ticket.setOriginalTicket(ticket1);
                    ticket.setIsDuplicate(true);
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "Parent Ticket cannot be marked as duplicate");
                    return map;
                }

            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "No ticket found for ticketId=" + ticketDto.getOriginalTicketId());
                return map;
            }
        }
        List<TicketUpdationHistory> historyList = new ArrayList<>();
        Timestamp timestamp = new Timestamp(new Date().getTime());
        if (ticketDto.getSubject() != null && !ticket.getSubject().equals(ticketDto.getSubject())) {
            historyList.add(createTicketUpdationHistory(ticket, user, "subject", ticket.getSubject(), ticketDto.getSubject(), timestamp));
            ticket.setSubject(ticketDto.getSubject());
        }
        if (ticketDto.getTypeId() != null && !ticket.getIncidentType().getId().equals(ticketDto.getTypeId())) {
            Optional<IncidentType> incidentTypeOptional = incidentTypeRepository.findById(ticketDto.getTypeId());
            if (incidentTypeOptional.isPresent()) {
                IncidentType newIncidentType = incidentTypeOptional.get();
                historyList.add(createTicketUpdationHistory(ticket, user, "type", ticket.getIncidentType().getType(), newIncidentType.getType(), timestamp));
                ticket.setIncidentType(newIncidentType);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.INCIDENT_TYPE_NOT_FOUND);
                return map;
            }

        }

        if (ticketDto.getPriority() != null && !ticket.getPriority().equals(ticketDto.getPriority())) {
            historyList.add(createTicketUpdationHistory(ticket, user, "priority", ticket.getPriority(), ticketDto.getPriority(), timestamp));
            ticket.setPriority(ticketDto.getPriority());
        }

        if (ticketDto.getIssueRelated() != null && !ticket.getIssueRelated().equals(ticketDto.getIssueRelated())) {
            historyList.add(createTicketUpdationHistory(ticket, user, "issueRelated", ticket.getIssueRelated(), ticketDto.getIssueRelated(), timestamp));
            ticket.setIssueRelated(ticketDto.getIssueRelated());
        }

        if (ticketDto.getStatus() != null && !ticket.getStatus().equals(ticketDto.getStatus())) {
            historyList.add(createTicketUpdationHistory(ticket, user, "status", ticket.getStatus(), ticketDto.getStatus(), timestamp));
            ticket.setStatus(ticketDto.getStatus());
        }
        if (ticketDto.getAssignedTo() != null) {
            Optional<User> assignedUserOptional = userRepository.findById(ticketDto.getAssignedTo());
            if (assignedUserOptional.isPresent()) {
                User assignedUser = assignedUserOptional.get();
                if (!ticket.getAssignedTo().equals(assignedUser)) {
                    historyList.add(createTicketUpdationHistory(ticket, user, "assignedTo", ticket.getAssignedTo().getUsername(), assignedUser.getUsername(), timestamp));
                    ticket.setAssignedTo(assignedUser);
                }
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "Assigned user not found");
                return map;
            }
        }
        ticket.setUpdatedBy(user);
        ticketUpdationHistoryRepository.saveAll(historyList);
        ticketRepository.save(ticket);
        Comment comment = new Comment();
        if (ticketDto.getComment() != null) {
            comment.setComment(ticketDto.getComment());
            comment.setUser(user);
            Date date1 = new Date();
            Timestamp timestamp1 = new Timestamp(date1.getTime());
            comment.setCreatedTime(timestamp1);
            comment.setTicket(ticket);
            commentRepository.save(comment);
        }

        if (ticketDto.getTicketFileDtos() != null && !ticketDto.getTicketFileDtos().isEmpty()) {
            for (TicketFileDto ticketFileDto : ticketDto.getTicketFileDtos()) {
                TicketFile ticketFile = new TicketFile();
                ticketFile.setOptionalDescription(ticketFileDto.getOptionalDescription());
                ticketFile.setTicket(ticket);
                MultipartFile multipartFile = ticketFileDto.getFile();
                if (multipartFile != null && !multipartFile.isEmpty()) {
                    try {
                        File baseFolder = new File(fileFolderPath);
                        if (!baseFolder.exists()) {
                            baseFolder.mkdirs();
                        }
                        String dateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        String ticketFolderName = String.valueOf(ticket.getId());
                        String directoryPath = baseFolder + File.separator + dateFolder + File.separator + ticketFolderName;
                        String originalFileName = multipartFile.getOriginalFilename();
                        String extension = getFileExtensionFromMimeType(multipartFile.getContentType());
                        if ("unknown".equals(extension)) {
                            int dotIndex = originalFileName.lastIndexOf('.');
                            if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                                extension = originalFileName.substring(dotIndex + 1).toLowerCase();
                            }
                        }
                        ticketFile.setFileType(extension);
                        String fileNameWithoutExtension;
                        int dotIndex = originalFileName.lastIndexOf('.');
                        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
                            fileNameWithoutExtension = originalFileName.substring(0, dotIndex);
                        } else {
                            fileNameWithoutExtension = originalFileName;
                        }

                        String filePath = directoryPath + File.separator + fileNameWithoutExtension;
                        String filePathToSave = File.separator + dateFolder + File.separator + ticketFolderName;
                        File directory = new File(directoryPath);
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        File file = new File(filePath);
                        file.getParentFile().mkdirs();
                        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            file.createNewFile();
                            fileOutputStream.write(multipartFile.getBytes());
                            fileOutputStream.close();
                        }
                        ticketFile.setFilePath(filePathToSave);
                        ticketFile.setFileType(extension);
                        ticketFile.setFileName(fileNameWithoutExtension);
                        if (comment != null) {
                            ticketFile.setComment(comment);
                        } else {
                            ticketFile.setComment(null);
                        }
                    } catch (IOException e) {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, "Failed to save the file.");
                        return map;
                    }
                    ticketFileRepository.save(ticketFile);
                }
            }
        }

        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        return map;
    }

    private String getFileExtensionFromMimeType(String mimeType) {
        Map<String, String> mimeTypeToExtensionMap = new HashMap<>();
        mimeTypeToExtensionMap.put("application/msword", "doc");
        mimeTypeToExtensionMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        mimeTypeToExtensionMap.put("application/pdf", "pdf");
        return mimeTypeToExtensionMap.getOrDefault(mimeType, "unknown");
    }


    private TicketUpdationHistory createTicketUpdationHistory(Ticket ticket, User user, String changeIn, String changeFrom, String changeTo, Timestamp timestamp) {
        TicketUpdationHistory history = new TicketUpdationHistory();
        history.setTicket(ticket);
        history.setUpdatedBy(user);
        history.setChangeIn(changeIn);
        history.setChangeFrom(changeFrom);
        history.setChangeTo(changeTo);
        history.setUpdatedTime(timestamp);
        return history;
    }

    @Override
    public HashMap<String, Object> removeTicket(Integer ticketId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            ticket.setAssignedTo(null);
            ticket.setCustomerProductMapping(null);
            ticket.setCustomerProductMappingDevice(null);
            ticketRepository.deleteById(ticketId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.TICKET_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> findTicketById(String token, Integer ticketId) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        Customer usersCustomer = user.getCustomer();
        Optional<Ticket> ticketOptional = ticketRepository.findOne((root, query, criteriaBuilder) -> {
            List<HashMap<String, Object>> ticketList = new ArrayList<>();
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("id"), ticketId));
            if (usersCustomer != null) {
                Join<Ticket, CustomerProductMapping> deviceCustomerProductMappingDeviceJoin = root.join("customerProductMappingDevice", JoinType.INNER);
                Join<CustomerProductMappingDevice, CustomerProductMapping> customerProductMappingDeviceCustomerProductMappingJoin = deviceCustomerProductMappingDeviceJoin.join("customerProductMapping", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(customerProductMappingDeviceCustomerProductMappingJoin.get("customer"), usersCustomer));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", ticket.getId());
            data.put("subject", ticket.getSubject());
            Optional<IncidentType> incidentTypeOptional = incidentTypeRepository.findById(ticket.getIncidentType().getId());
            if (incidentTypeOptional.isPresent()) {
                IncidentType incidentType = incidentTypeOptional.get();
                data.put("type", incidentType.getType());
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.INCIDENT_TYPE_NOT_FOUND);
                return map;
            }
            data.put("issueRelated", ticket.getIssueRelated());
            data.put("priority", ticket.getPriority());
            data.put("status", ticket.getStatus());
            data.put("description", ticket.getDescription());
            Timestamp timestamp = ticket.getCreatedTime();
            Date date = new Date(timestamp.getTime());
            data.put("createdTime", date);
            Map<String, Object> userDetails = new HashMap<>();
            Optional<User> userOptional = userRepository.findById(ticket.getAssignedTo().getId());
            if (userOptional.isPresent()) {
                User user1 = userOptional.get();
                userDetails.put("id", user1.getId());
                userDetails.put("userName", user1.getUsername());
            }
            data.put("assignedTo", userDetails);
            Map<String, Object> customerDetails = new HashMap<>();
            Optional<Customer> customerOptional = customerRepository.findById(ticket.getCustomerProductMapping().getCustomer().getId());
            if (customerOptional.isPresent()) {
                Customer customer = customerOptional.get();
                customerDetails.put("id", customer.getId());
                customerDetails.put("name", customer.getName());
            }
            data.put("customer", customerDetails);
            Map<String, Object> productDetails = new HashMap<>();
            Optional<Product> productOptional = productRepository.findById(ticket.getCustomerProductMapping().getProduct().getId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                productDetails.put("id", product.getId());
                productDetails.put("productName", product.getName());
            }
            data.put("product", productDetails);
            Map<String, Object> deviceDetails = new HashMap<>();
            Optional<Device> deviceOptional = deviceRepository.findById(ticket.getCustomerProductMappingDevice().getDevice().getId());
            if (deviceOptional.isPresent()) {
                Device device = deviceOptional.get();
                deviceDetails.put("id", device.getId());
                deviceDetails.put("name", device.getName());
            }
            data.put("device", deviceDetails);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.DATA, data);
            return map;

        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.TICKET_NOT_FOUND);
            return map;
        }
    }
}
