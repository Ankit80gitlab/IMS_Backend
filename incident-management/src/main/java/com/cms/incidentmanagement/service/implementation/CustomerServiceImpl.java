package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.CustomerDto;
import com.cms.incidentmanagement.dto.IncidentTypeDto;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.service.CustomerService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shashidhar on 4/15/2024.
 */
@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private Utilities utility;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private CustomerProductMappingRepository customerProductMappingRepository;
    @Autowired
    private UserProductMappingRepository userProductMappingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IncidentTypeRepository incidentTypeRepository;
    @Autowired
    private IncidentEscalationRepository incidentEscalationRepository;
    @Autowired
    private IncidentEscalationUserRepository incidentEscalationUserRepository;
    @Autowired
    private IncidentEscalationDefaultUserRepository incidentEscalationDefaultUserRepository;

    @Override
    public HashMap<String, Object> addCustomer(CustomerDto customerDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = customerRepository.countByNameIgnoreCase(customerDto.getName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_ALREADY_EXISTS);
            return map;
        }
        Customer customer = new Customer();
        customer.setName(customerDto.getName().trim());
        customer.setState(customerDto.getState());
        customer.setCity(customerDto.getCity());
        User loggedInUser = utility.getLoggedInUser(token);
        customer.setUser(loggedInUser);
        List<Integer> productIds = customerDto.getProductIds();
        Set<CustomerProductMapping> customerProductMappings = new HashSet<>();
        for (Integer productId : productIds) {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product1 = productOptional.get();
                CustomerProductMapping customerProductMapping = new CustomerProductMapping();
                customerProductMapping.setCustomer(customer);
                customerProductMapping.setProduct(product1);
                customerProductMappings.add(customerProductMapping);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND + "for productId=" + productId);
                return map;
            }
        }
        customer.setCustomerProductMappings(customerProductMappings);
        Customer savedCustomer = customerRepository.save(customer);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.ADD_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedCustomer.getId());
        }});

        return map;
    }

    @Override
    public HashMap<String, Object> getAllCustomers(Integer pageNo, Integer pageSize, String token, String searchByName) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> customerList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Customer> customerPage = customerRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root, userCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        customerPage.stream().forEach(customer -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", customer.getId());
            data.put("name", customer.getName());
            data.put("state", customer.getState());
            data.put("city", customer.getCity());
            List<Map<String, Object>> products = customer.getCustomerProductMappings().stream()
                    .map(mapping -> mapping.getProduct())
                    .map(product -> {
                        Map<String, Object> productDetails = new HashMap<>();
                        productDetails.put("id", product.getId());
                        productDetails.put("productName", product.getName());
                        return productDetails;
                    })
                    .collect(Collectors.toList());
            data.put("products", products);
            customerList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, customerList);
        return map;
    }


    @Override
    public HashMap<String, Object> updateCustomer(CustomerDto customerDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Customer> customerOptional = customerRepository.findById(customerDto.getId());
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            customer.setName(customerDto.getName().trim());
            customer.setState(customerDto.getState());
            customer.setCity(customerDto.getCity());
            User loggedInUser = utility.getLoggedInUser(token);
            List<Integer> productIds = customerDto.getProductIds();
            List<CustomerProductMapping> existingCustomerProductMappings = customerProductMappingRepository.findByCustomerId(customerDto.getId());
            List<CustomerProductMapping> newCustomerProductMapping = customerProductMappingRepository.findByCustomerIdAndProductIdIn(customerDto.getId(), productIds);
            List<Integer> newCustomerProductMappingIds = newCustomerProductMapping.stream().map(mapping -> mapping.getId()).collect(Collectors.toList());
            List<Integer> existingCustomerProductMappingIds = existingCustomerProductMappings.stream().map(mapping -> mapping.getId()).collect(Collectors.toList());
            for (CustomerProductMapping existingCustomerProductMapping : existingCustomerProductMappings) {
                if (!newCustomerProductMappingIds.contains(existingCustomerProductMapping.getId())) {
                    customerProductMappingRepository.delete(existingCustomerProductMapping);
                }
            }
            for (Integer productId : productIds) {
                Optional<Product> productOptional = productRepository.findById(productId);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    Optional<CustomerProductMapping> existingCustomerProductMappingOptional = customerProductMappingRepository.findByCustomerIdAndProductId(customerDto.getId(), product.getId());
                    CustomerProductMapping customerProductMapping;
                    if (existingCustomerProductMappingOptional.isPresent()) {
                        customerProductMapping = existingCustomerProductMappingOptional.get();
                    } else {
                        customerProductMapping = new CustomerProductMapping();
                        customerProductMapping.setCustomer(customer);
                        customerProductMapping.setProduct(product);
                        customerProductMappingRepository.save(customerProductMapping);
                    }
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "Product not found for ID: " + productId);
                    return map;
                }
            }
            customerRepository.save(customer);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
        }

        return map;
    }


    @Override
    public HashMap<String, Object> removeCustomer(Integer customerId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            List<CustomerProductMapping> customerProductMappings = customerProductMappingRepository.findByCustomerId(customerId);
            if (customerProductMappings != null) {
                List<Integer> customerProductMappingIds = customerProductMappings.stream().map(mapping -> mapping.getId()).collect(Collectors.toList());
                List<Ticket> tickets = ticketRepository.findAllByCustomerProductMappingIdIn(customerProductMappingIds);
                if (tickets.isEmpty()) {
                    customerRepository.deleteById(customerId);
                    map.put(Constant.STATUS, Constant.SUCCESS);
                    map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "Customer cannot be deleted because of pending tickets");
                    return map;
                }
            } else {
                customerRepository.deleteById(customerId);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getAllCustomersBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> customerList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Customer> customerPage = customerRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root, userCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        customerPage.stream().forEach(customer -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", customer.getId());
            data.put("name", customer.getName());
            customerList.add(data);
        });
        if (userCustomer != null) {
            map.put("customer_id", userCustomer.getId());
        } else {
            map.put("customer_id", null);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, customerList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllProductsOfCustomer(String token, Integer pageNo, Integer pageSize, Integer customerId) {
        HashMap<String, Object> map = new HashMap<>();
        List<CustomerProductMapping> customerProductMappings = customerProductMappingRepository.findBycustomerId(customerId);
        List<Integer> productIds = customerProductMappings.stream()
                .map(mapping -> mapping.getProduct().getId())
                .collect(Collectors.toList());
        List<Map<String, Object>> productDataList = new ArrayList<>();
        for (Integer productId : productIds) {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                HashMap<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("name", product.getName());
                productDataList.add(productData);
            }
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, productDataList);
        return map;
    }

    @Override
    public HashMap<String, Object> getCustomersProductDetails(String token, Integer pageNo, Integer pageSize, Integer customerId, Integer productId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findByCustomerIdAndProductId(customerId, productId);
        if (customerProductMappingOptional.isPresent()) {
            Product product = productRepository.findById(productId).get();
            Map<String, Object> productDetails = new HashMap<>();
            productDetails.put("id", product.getId());
            productDetails.put("productName", product.getName());
            Integer customerProductMappingId = customerProductMappingOptional.get().getId();
            List<UserProductMapping> userProductMappings = userProductMappingRepository.findByCustomerProductMappingId(customerProductMappingId);
            List<Map<String, Object>> users = userProductMappings.stream().map(mapping -> mapping.getUser()).map(user1 -> {
                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("id", user1.getId());
                userDetails.put("userName", user1.getUsername());
                return userDetails;
            }).collect(Collectors.toList());
            if (users == null) {
                productDetails.put("Users", Collections.emptyList());
            } else {
                productDetails.put("Users", users);
            }
            Set<IncidentType> incidentTypes = product.getIncidentTypes();
            List<Map<String, Object>> incidentTypesDto = incidentTypes.stream().map(mapping -> {
                Map<String, Object> incidentTypeDetails = new HashMap<>();
                Optional<IncidentEscalation> incidentEscalationOptional = incidentEscalationRepository.findByIncidentTypeIdAndCustomerProductMappingId(mapping.getId(), customerProductMappingId);
                if (incidentEscalationOptional.isPresent()) {
                    IncidentEscalation incidentEscalation = incidentEscalationOptional.get();
                    incidentTypeDetails.put("id", mapping.getId());
                    incidentTypeDetails.put("type", mapping.getType());
                    incidentTypeDetails.put("escalationHour", incidentEscalation.getEscalationHour());
                    List<Map<String, Object>> defaultUserList = incidentEscalation.getIncidentEscalationDefaultUsers().stream().map(mapping1 -> mapping1.getUser()).map(user2 -> {
                        Map<String, Object> defaultUserDetails = new HashMap<>();
                        defaultUserDetails.put("id", user2.getId());
                        defaultUserDetails.put("userName", user2.getUsername());
                        return defaultUserDetails;
                    }).collect(Collectors.toList());
                    if (defaultUserList == null) {
                        incidentTypeDetails.put("defaultUser", Collections.emptyList());
                    } else {
                        incidentTypeDetails.put("defaultUser", defaultUserList);
                    }
                    incidentTypeDetails.put("defaultUser", defaultUserList);
                    List<Map<String, Object>> escalatedUserList = incidentEscalation.getIncidentEscalationUsers().stream().map(mapping2 -> mapping2.getUser()).map(user3 -> {
                        Map<String, Object> EscalatedUserDetails = new HashMap<>();
                        EscalatedUserDetails.put("id", user3.getId());
                        EscalatedUserDetails.put("userName", user3.getUsername());
                        return EscalatedUserDetails;
                    }).collect(Collectors.toList());
                    if (escalatedUserList == null) {
                        incidentTypeDetails.put("escalatedUser", Collections.emptyList());

                    } else {
                        incidentTypeDetails.put("escalatedUser", escalatedUserList);
                    }
                }
                return incidentTypeDetails;
            }).collect(Collectors.toList());
            List<Map<String, Object>> nonEmptyIncidentTypesDto = incidentTypesDto.stream()
                    .filter(map1 -> !map1.isEmpty())
                    .collect(Collectors.toList());
            if (nonEmptyIncidentTypesDto.isEmpty()) {
                productDetails.put("IncidentTypesDto", Collections.emptyList());
            } else {
                productDetails.put("IncidentTypesDto", nonEmptyIncidentTypesDto);
            }
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.DATA, productDetails);
            return map;


        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "Please save the Product to the Customer before adding the product details!");
            return map;
        }

    }

    @Override
    public HashMap<String, Object> updateCustomerProductDetails(ProductDto productDto, String token, Integer customerId) {
        HashMap<String, Object> map = new HashMap<>();
        User loggedInUser = utility.getLoggedInUser(token);
        Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findByCustomerIdAndProductId(customerId, productDto.getId());
        if (customerProductMappingOptional.isPresent()) {
            CustomerProductMapping customerProductMapping = customerProductMappingOptional.get();
            if (productDto.getUserIds() != null) {
                List<UserProductMapping> existingUserProductMappings = userProductMappingRepository.findByCustomerProductMappingId(customerProductMapping.getId());
                List<User> existingUsers = existingUserProductMappings.stream().map(UserProductMapping::getUser).collect(Collectors.toList());
                List<Integer> existingUserIds = existingUsers.stream().map(User::getId).collect(Collectors.toList());
                List<User> usersToAdd = userRepository.findAllById(productDto.getUserIds());
                List<Integer> usersToAddIds = usersToAdd.stream().map(User::getId).collect(Collectors.toList());
                for (User userToAdd : usersToAdd) {
                    if (!existingUserIds.contains(userToAdd.getId())) {
                        UserProductMapping userProductMapping = new UserProductMapping();
                        userProductMapping.setUser(userToAdd);
                        userProductMapping.setCustomerProductMapping(customerProductMapping);
                        userProductMappingRepository.save(userProductMapping);
                    }
                }
                for (UserProductMapping existingUserProductMapping : existingUserProductMappings) {
                    if (!usersToAddIds.contains(existingUserProductMapping.getUser().getId())) {
                        userProductMappingRepository.delete(existingUserProductMapping);
                    }
                }
            }
            List<IncidentTypeDto> incidentTypeDtos = productDto.getIncidentTypeDtos();
            if (incidentTypeDtos != null) {
                List<Integer> incidentTypeIds = incidentTypeDtos.stream().map(mapping -> mapping.getId()).collect(Collectors.toList());
                long uniqueCount = incidentTypeIds.stream().distinct().count();
                if (uniqueCount == incidentTypeIds.size()) {
                    List<Integer> sentIncidentTypeIds = incidentTypeDtos.stream().map(IncidentTypeDto::getId)
                            .collect(Collectors.toList());
                    List<IncidentType> existingIncidentTypesOfProduct = incidentTypeRepository.findByProductId(productDto.getId());
                    List<Integer> existingIncidentTypeIdsOfProduct = existingIncidentTypesOfProduct.stream().map(mapping -> mapping.getId()).collect(Collectors.toList());
                    for (Integer sentIncidentTypeId : sentIncidentTypeIds) {
                        if (!(existingIncidentTypeIdsOfProduct).contains(sentIncidentTypeId)) {
                            map.put(Constant.STATUS, Constant.ERROR);
                            map.put(Constant.MESSAGE, "IncidentType with ID " + sentIncidentTypeId + " is not associated with the product");
                            return map;
                        }

                    }
                    List<IncidentEscalation> existingIncidentEscalations = incidentEscalationRepository
                            .findByCustomerProductMappingId(customerProductMapping.getId());
                    for (IncidentEscalation existingEscalation : existingIncidentEscalations) {
                        if (!sentIncidentTypeIds.contains(existingEscalation.getIncidentType().getId())) {
                            incidentEscalationRepository.delete(existingEscalation);
                        }
                    }
                    for (IncidentTypeDto incidentTypeDto : incidentTypeDtos) {
                        Optional<IncidentType> incidentTypeOptional = incidentTypeRepository.findById(incidentTypeDto.getId());
                        if (incidentTypeOptional.isPresent()) {
                            IncidentType incidentType1 = incidentTypeOptional.get();
                            Optional<IncidentEscalation> existingIncidentEscalation = incidentEscalationRepository.findByIncidentTypeIdAndCustomerProductMappingId(incidentType1.getId(), customerProductMapping.getId());
                            IncidentEscalation incidentEscalation;
                            if (existingIncidentEscalation.isPresent()) {
                                incidentEscalation = existingIncidentEscalation.get();
                                incidentEscalation.setEscalationHour(incidentTypeDto.getEscalationHour());
                            } else {
                                incidentEscalation = new IncidentEscalation();
                                incidentEscalation.setEscalationHour(incidentTypeDto.getEscalationHour());
                                incidentEscalation.setUser(loggedInUser);
                                incidentEscalation.setIncidentType(incidentType1);
                                incidentEscalation.setCustomerProductMapping(customerProductMapping);
                                incidentEscalationRepository.save(incidentEscalation);
                            }
                            List<IncidentEscalationDefaultUser> existingIncidentEscalationDefaultUsers = incidentEscalationDefaultUserRepository.findByIncidentEscalationId(incidentEscalation.getId());
                            List<Integer> existingIncidentEscalationDefaultUserId = existingIncidentEscalationDefaultUsers.stream().map(mapping -> mapping.getUser().getId()).collect(Collectors.toList());
                            List<User> incidentEscalationDefaultUsersToAdd = userRepository.findAllById(incidentTypeDto.getDefaultUserIds());
                            List<Integer> incidentEscalationDefaultUsersToAddIds = incidentEscalationDefaultUsersToAdd.stream().map(User::getId).collect(Collectors.toList());
                            for (User defaultUserToAdd : incidentEscalationDefaultUsersToAdd) {
                                if (!existingIncidentEscalationDefaultUserId.contains(defaultUserToAdd.getId())) {
                                    IncidentEscalationDefaultUser incidentEscalationDefaultUser = new IncidentEscalationDefaultUser();
                                    incidentEscalationDefaultUser.setUser(defaultUserToAdd);
                                    incidentEscalationDefaultUser.setIncidentEscalation(incidentEscalation);
                                    incidentEscalationDefaultUserRepository.save(incidentEscalationDefaultUser);
                                }
                            }
                            for (IncidentEscalationDefaultUser existingIncidentEscalationDefaultUser : existingIncidentEscalationDefaultUsers) {
                                if (!incidentEscalationDefaultUsersToAddIds.contains(existingIncidentEscalationDefaultUser.getUser().getId())) {
                                    incidentEscalationDefaultUserRepository.delete(existingIncidentEscalationDefaultUser);
                                }
                            }
                            List<IncidentEscalationUser> existingIncidentEscalationUsers = incidentEscalationUserRepository.findByIncidentEscalationId(incidentEscalation.getId());
                            List<Integer> existingIncidentEscalationUserId = existingIncidentEscalationUsers.stream().map(mapping -> mapping.getUser().getId()).collect(Collectors.toList());
                            List<User> incidentEscalationUsersToAdd = userRepository.findAllById(incidentTypeDto.getIncidentEscalationUserIds());
                            List<Integer> incidentEscalationUsersToAddIds = incidentEscalationUsersToAdd.stream().map(User::getId).collect(Collectors.toList());
                            for (User userToAdd : incidentEscalationUsersToAdd) {
                                if (!existingIncidentEscalationUserId.contains(userToAdd.getId())) {
                                    IncidentEscalationUser incidentEscalationUser = new IncidentEscalationUser();
                                    incidentEscalationUser.setUser(userToAdd);
                                    incidentEscalationUser.setIncidentEscalation(incidentEscalation);
                                    incidentEscalationUserRepository.save(incidentEscalationUser);
                                }
                            }
                            for (IncidentEscalationUser existingIncidentEscalationUser : existingIncidentEscalationUsers) {
                                if (!incidentEscalationUsersToAddIds.contains(existingIncidentEscalationUser.getUser().getId())) {
                                    incidentEscalationUserRepository.delete(existingIncidentEscalationUser);
                                }
                            }
                        } else {
                            map.put(Constant.STATUS, Constant.ERROR);
                            map.put(Constant.MESSAGE, "IncidentType not found for ID: " + incidentTypeDto.getId());
                            return map;
                        }
                    }

                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "Two incident type should not be same");
                    return map;

                }
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "Product is not associated with the customer");
            return map;
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        return map;
    }

    @Override
    public HashMap<String, Object> removeCustomersProduct(String token, Integer customerId, Integer productId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findByCustomerIdAndProductId(customerId, productId);
        if (customerProductMappingOptional.isPresent()) {
            CustomerProductMapping customerProductMapping = customerProductMappingOptional.get();
            Integer customerProductMappingId = customerProductMapping.getId();
            customerProductMappingRepository.deleteById(customerProductMappingId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "Product is not associated with customer");
        }
        return map;
    }
}

