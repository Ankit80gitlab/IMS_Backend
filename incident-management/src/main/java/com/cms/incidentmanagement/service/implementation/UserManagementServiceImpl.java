package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.entity.UserType;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.UserDto;
import com.cms.incidentmanagement.service.UserManagementService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManagementServiceImpl implements UserManagementService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private Utilities utility;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerProductMappingRepository customerProductMappingRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserTypeRepository userTypeRepository;

    public HashMap<String, Object> getAllUsers(Integer pageNo, Integer pageSize, String token, String searchByUsername) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> userList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"username"}).ascending());
        Page<User> userPage = userRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), userCustomer));
            }
            if (searchByUsername != null && !searchByUsername.isEmpty()) {
                String searchTerm = "%" + searchByUsername.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        userPage.stream().forEach(user1 -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", user1.getId());
            data.put("userName", user1.getUsername());
            data.put("name", user1.getName());
            data.put("email", user1.getEmail());
            data.put("lat", user1.getLat());
            data.put("lon", user1.getLon());
            data.put("roleId", user1.getUserRoleMappings().stream().findFirst().get().getRole().getId());
            Customer user1Customer = user1.getCustomer();
            if (user1Customer != null) {
                data.put("customerId", user1Customer.getId());
                data.put("customerName", user1Customer.getName());
                List<CustomerProductMapping> customerProductMappings = user1.getUserProductMappings().stream().map(mapping -> mapping.getCustomerProductMapping()).collect(Collectors.toList());
                data.put("productIds", customerProductMappings.stream()
                        .map(mapping -> mapping.getProduct().getId())
                        .collect(Collectors.toList()));
            } else {
                data.put("customerId", null);
                data.put("customerName", null);
                data.put("productIds", Collections.emptyList());
            }
            Integer userTypeId = user1.getUserType() != null ? user1.getUserType().getId() : null;
            data.put("userTypeId", userTypeId);
            userList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, userList);
        return map;
    }

    public HashMap<String, Object> updateUser(UserDto userDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<User> optionalUser = userRepository.findById(userDto.getId());
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            if ("ImsAdmin".equals(existingUser.getUsername())) {
                userDto.setIsEditable(false);
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "User 'ImsAdmin' is not editable");
                return map;
            } else {
                if (existingUser.getUsername().equalsIgnoreCase(userDto.getUserName())) {
                    existingUser.setUsername(userDto.getUserName().trim());
                } else {
                    long existingZoneCount = userRepository.countByUsernameIgnoreCase(userDto.getUserName().trim());
                    if (existingZoneCount > 0) {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, Constant.USERNAME_ALREADY_EXIST);
                        return map;
                    } else {
                        existingUser.setUsername(userDto.getUserName().trim());
                    }
                }
                if (existingUser.getEmail().equalsIgnoreCase(userDto.getEmail().trim())) {
                    existingUser.setEmail(userDto.getEmail().trim());
                } else {
                    long existingEmailCount = userRepository.countByEmailIgnoreCase(userDto.getEmail().trim());
                    if (existingEmailCount > 0) {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, Constant.EMAIL_ALREADY_EXIST);
                        return map;
                    } else {
                        existingUser.setEmail(userDto.getEmail().trim());
                    }
                }
                existingUser.setName(userDto.getName());
                existingUser.setLat(userDto.getLat());
                existingUser.setLon(userDto.getLon());
                if (userDto.getCustomerId() != null) {
                    Optional<Customer> optionalCustomer = customerRepository.findById(userDto.getCustomerId());
                    if (optionalCustomer.isPresent()) {
                        Set<UserProductMapping> userProductMappings = existingUser.getUserProductMappings();
                        Integer existingUsersCustomerId = existingUser.getCustomer().getId();
                        if (!userProductMappings.isEmpty()) {
                            if (userDto.getCustomerId() != existingUsersCustomerId) {
                                map.put(Constant.STATUS, Constant.ERROR);
                                map.put(Constant.MESSAGE, "Customer cannot be changed because of users association with another customer and its product");
                                return map;
                            } else {
                                Customer customer = optionalCustomer.get();
                                existingUser.setCustomer(customer);
                            }

                        } else {
                            Customer customer = optionalCustomer.get();
                            existingUser.setCustomer(customer);
                        }
                    } else {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
                        return map;
                    }
                } else {
                    existingUser.setCustomer(null);
                }
                if (userDto.getUserTypeId() != null) {
                    Optional<UserType> userTypeOptional = userTypeRepository.findById(userDto.getUserTypeId());
                    if (userTypeOptional.isPresent()) {
                        UserType userType = userTypeOptional.get();
                        existingUser.setUserType(userType);
                    } else {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, Constant.TYPE_NOT_FOUND);
                        return map;
                    }
                } else {
                    existingUser.setUserType(null);
                }
                Optional<Role> optionalRole = roleRepository.findById(userDto.getRoleId());
                if (optionalRole.isPresent()) {
                    Role newRole = optionalRole.get();
                    User user = utility.getLoggedInUser(token);
                    String roleName = user.getUserRoleMappings().stream().findFirst().get().getRole().getName();
                    Set<UserRoleMapping> existingMappings = existingUser.getUserRoleMappings();
                    Set<Integer> existingRoleIds = existingMappings.stream()
                            .map(mapping -> mapping.getRole().getId())
                            .collect(Collectors.toSet());
                    Set<Integer> newRoleIds = Collections.singleton(newRole.getId());
                    Set<Integer> rolesToRemove = new HashSet<>(existingRoleIds);
                    rolesToRemove.removeAll(newRoleIds);
                    existingMappings.removeIf(mapping -> rolesToRemove.contains(mapping.getRole().getId()));
                    if (!existingRoleIds.contains(newRole.getId())) {
                        UserRoleMapping newUserRoleMapping = new UserRoleMapping();
                        newUserRoleMapping.setUser(existingUser);
                        newUserRoleMapping.setRole(newRole);
                        existingUser.getUserRoleMappings().add(newUserRoleMapping);
                    }
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, Constant.ROLE_ID_NOT_FOUND);
                    return map;
                }
                if (userDto.getProductIds() != null) {
                    if (userDto.getProductIds().isEmpty()) {
                        existingUser.getUserProductMappings().clear();
                    } else {
                        List<Product> allProducts = productRepository.findAll();
                        Set<Integer> validProductIds = allProducts.stream().map(Product::getId).collect(Collectors.toSet());
                        for (Integer productId : userDto.getProductIds()) {
                            if (!validProductIds.contains(productId)) {
                                map.put(Constant.STATUS, Constant.ERROR);
                                map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND + " for productId=" + productId);
                                return map;
                            }
                        }
                        if (userDto.getCustomerId() != null) {
                            List<CustomerProductMapping> customerProductMappingList = customerProductMappingRepository.findByCustomerIdAndProductIdIn(userDto.getCustomerId(), userDto.getProductIds());
                            if (customerProductMappingList == null || customerProductMappingList.isEmpty()) {
                                map.put(Constant.STATUS, Constant.ERROR);
                                map.put(Constant.MESSAGE, "No Products are Associated for the CustomerId=" + userDto.getCustomerId());
                                return map;
                            }
                            Set<Integer> associatedProductIds = customerProductMappingList.stream()
                                    .map(mapping -> mapping.getProduct().getId())
                                    .collect(Collectors.toSet());
                            for (Integer productId : userDto.getProductIds()) {
                                if (!associatedProductIds.contains(productId)) {
                                    map.put(Constant.STATUS, Constant.ERROR);
                                    map.put(Constant.MESSAGE, "Customer does not have access to Product with ProductId=" + productId);
                                    return map;
                                }
                            }
                            Set<UserProductMapping> existingMappings = existingUser.getUserProductMappings();
                            Set<Integer> existingCustomerProductIds = existingMappings.stream()
                                    .map(mapping -> mapping.getCustomerProductMapping().getId())
                                    .collect(Collectors.toSet());
                            Set<Integer> newCustomerProductIds = customerProductMappingList.stream()
                                    .map(CustomerProductMapping::getId)
                                    .collect(Collectors.toSet());
                            existingMappings.removeIf(mapping -> !newCustomerProductIds.contains(mapping.getCustomerProductMapping().getId()));
                            for (Integer customerProductId : newCustomerProductIds) {
                                if (!existingCustomerProductIds.contains(customerProductId)) {
                                    Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findById(customerProductId);
                                    if (customerProductMappingOptional.isPresent()) {
                                        CustomerProductMapping customerProductMapping = customerProductMappingOptional.get();
                                        UserProductMapping newMapping = new UserProductMapping();
                                        newMapping.setUser(existingUser);
                                        newMapping.setCustomerProductMapping(customerProductMapping);
                                        existingMappings.add(newMapping);
                                    }
                                }
                            }
                        }
                    }
                }
                userRepository.save(existingUser);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
            }

        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
        }
        return map;
    }

    public HashMap<String, Object> changePassword(String token, String currentPassword, String newPassword) {
        HashMap<String, Object> map = new HashMap<>();
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        User user = utility.getLoggedInUser(token);
        if (user != null) {
            if (bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
                if (!currentPassword.equalsIgnoreCase(newPassword)) {
                    User updatedPassUser = userRepository.findById(user.getId()).get();
                    updatedPassUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
                    userRepository.save(updatedPassUser);
                    map.put(Constant.STATUS, Constant.SUCCESS);
                    map.put(Constant.MESSAGE, Constant.PASSWORD_CHANGED_SUCCESS);
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, Constant.SAME_OLD_NEW_PASSWORD);
                }
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.INCORRECT_PASSWORD);
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
        }
        return map;
    }


    public HashMap<String, Object> removeUser(Integer userId, String token) {
        HashMap<String, Object> map = new HashMap<>();
        User loggedInUser = utility.getLoggedInUser(token);
        if (loggedInUser.getId().equals(userId)) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "User cannot delete themselves");
            return map;
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            User user1 = user.get();
            if ("ImsAdmin".equals(user1.getUsername())) {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "User 'ImsAdmin' cannot be Deleted");
                return map;
            }
            Set<UserProductMapping> userProductMappings = user1.getUserProductMappings();
            if (userProductMappings.isEmpty()) {
                userRepository.deleteById(userId);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "User cannot be deleted because user has asociation with a customer and with one or more product");
                return map;
            }

        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> registerUser(UserDto userDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = userRepository.countByUsernameIgnoreCase(userDto.getUserName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.USERNAME_ALREADY_EXIST);
            return map;
        }
        long existingEmailCount = userRepository.countByEmailIgnoreCase(userDto.getEmail());
        if (existingEmailCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.EMAIL_ALREADY_EXIST);
            return map;
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setUsername(userDto.getUserName().trim());
        String newPass = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(newPass);
        user.setLdapUser(false);
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        user.setCreatedTime(timestamp);
        user.setLoginTime(null);
        User loggedInUser = utility.getLoggedInUser(token);
        user.setUser(loggedInUser);
        user.setLat(userDto.getLat());
        user.setLon(userDto.getLon());
        Integer userTypeId = userDto.getUserTypeId();
        if (userTypeId != null) {
            Optional<UserType> userTypeOptional = userTypeRepository.findById(userDto.getUserTypeId());
            if (!userTypeOptional.isPresent()) {
                user.setUserType(null);
            } else {
                UserType userType = userTypeOptional.get();
                user.setUserType(userType);
            }

        }
        Optional<Role> optionalRole = roleRepository.findById(userDto.getRoleId());
        if (optionalRole.isPresent()) {
            Role assignedRole = optionalRole.get();
            Set<UserRoleMapping> userRoleMappings = new HashSet<>();
            UserRoleMapping userRoleMapping = new UserRoleMapping();
            userRoleMapping.setRole(assignedRole);
            userRoleMapping.setUser(user);
            userRoleMappings.add(userRoleMapping);
            user.setUserRoleMappings(userRoleMappings);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ROLE_NOT_FOUND);
            return map;
        }
        if (userDto.getCustomerId() != null) {
            Optional<Customer> optionalCustomer = customerRepository.findById(userDto.getCustomerId());
            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();
                user.setCustomer(customer);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
                return map;
            }
        } else {
            user.setCustomer(null);
        }

        List<Integer> productIds = userDto.getProductIds();
        Set<UserProductMapping> userProductMappings = new HashSet<>();
        if (productIds != null && !productIds.isEmpty()) {
            List<Product> allProducts = productRepository.findAll();
            for (Integer productId : productIds) {
                boolean productFound = allProducts.stream()
                        .anyMatch(product -> product.getId().equals(productId));

                if (!productFound) {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND + " for productId=" + productId);
                    return map;
                }
            }
            if (userDto.getCustomerId() != null) {
                List<CustomerProductMapping> customerProductMappingList = customerProductMappingRepository.findByCustomerIdAndProductIdIn(userDto.getCustomerId(), userDto.getProductIds());
                if (customerProductMappingList != null && !customerProductMappingList.isEmpty()) {
                    Set<Integer> associatedProductIds = customerProductMappingList.stream()
                            .map(mapping -> mapping.getProduct().getId())
                            .collect(Collectors.toSet());
                    for (Integer productId : userDto.getProductIds()) {
                        if (!associatedProductIds.contains(productId)) {
                            map.put(Constant.STATUS, Constant.ERROR);
                            map.put(Constant.MESSAGE, "Customer does not have access to Product with ProductId=" + productId);
                            return map;
                        }
                    }
                    for (CustomerProductMapping customerProductMapping : customerProductMappingList) {
                        UserProductMapping userProductMapping = new UserProductMapping();
                        userProductMapping.setUser(user);
                        userProductMapping.setCustomerProductMapping(customerProductMapping);
                        userProductMappings.add(userProductMapping);
                    }
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "No Products are Associated for the CustomerId=" + userDto.getCustomerId());
                    return map;
                }
            }
        }
        user.setUserProductMappings(userProductMappings);
        User savedUser = userRepository.save(user);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.REGISTERED_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedUser.getId());
        }});
        return map;
    }

    @Override
    public HashMap<String, Object> getAllUserBasicDetails(String token, String searchByUsername, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> userList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"username"}).ascending());
        Page<User> userPage = userRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), userCustomer));
            }
            if (searchByUsername != null && !searchByUsername.isEmpty()) {
                String searchTerm = "%" + searchByUsername.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        userPage.stream().forEach(user1 -> {
            HashMap<String, Object> data = new HashMap<>();
            Customer user1Customer = user1.getCustomer();
            data.put("customerId", user1Customer.getId());
            data.put("id", user1.getId());
            data.put("userName", user1.getUsername());
            userList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, userList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllCustomersUser(Integer pageNo, Integer pageSize, String token, String searchByUsername) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> userList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"username"}).ascending());
        Page<User> userPage = userRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isNotNull(root.get("customer")));
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), userCustomer));
            }
            if (searchByUsername != null && !searchByUsername.isEmpty()) {
                String searchTerm = "%" + searchByUsername.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        userPage.stream().forEach(user1 -> {
            HashMap<String, Object> data = new HashMap<>();
            Customer user1Customer = user1.getCustomer();
            if (user1Customer != null) {
                data.put("id", user1.getId());
                data.put("userName", user1.getUsername());
                data.put("name", user1.getName());
                data.put("email", user1.getEmail());
                data.put("lat", user1.getLat());
                data.put("lon", user1.getLon());
                data.put("roleId", user1.getUserRoleMappings().stream().findFirst().get().getRole().getId());
                data.put("customerId", user1Customer.getId());
                data.put("customerName", user1Customer.getName());
                data.put("productIds", user1Customer.getCustomerProductMappings().stream()
                        .map(mapping -> mapping.getProduct().getId())
                        .collect(Collectors.toList()));
            }
            userList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, userList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllUsersOfCustomer(Integer pageNo, Integer pageSize, String searchByUsername, Integer customerId) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> userList = new ArrayList<>();
        Optional<Customer> customerOptional = customerRepository.findById(customerId);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
            Page<User> userPage = userRepository.findAll((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("customer"), customer));
                if (searchByUsername != null && !searchByUsername.isEmpty()) {
                    String searchTerm = "%" + searchByUsername.toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchTerm));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }, pageRequest);
            userPage.stream().forEach(user -> {
                HashMap<String, Object> data = new HashMap<>();
                data.put("id", user.getId());
                data.put("userName", user.getUsername());
                List<Product> products = user.getUserProductMappings().stream().map(mapping -> mapping.getCustomerProductMapping().getProduct()).collect(Collectors.toList());
                List<HashMap<String, Object>> productDetailsList = new ArrayList<>();
                for (Product product : products) {
                    HashMap<String, Object> productData = new HashMap<>();
                    productData.put("id", product.getId());
                    productData.put("name", product.getName());
                    productDetailsList.add(productData);
                }
                data.put("products", productDetailsList);
                userList.add(data);
            });
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.DATA, userList);
            return map;

        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
            return map;
        }
    }

    @Override
    public HashMap<String, Object> getAllTypesForUser(Integer pageNo, Integer pageSize, String searchByName) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> userTypeList = new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<UserType> userTypePage = userTypeRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        userTypePage.stream().forEach(userType -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", userType.getId());
            data.put("name", userType.getName());
            userTypeList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, userTypeList);
        return map;
    }
}



