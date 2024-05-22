package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.dto.RoleDto;
import com.cms.incidentmanagement.dto.UserDto;
import com.cms.incidentmanagement.service.UserManagementService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private UserProductMappingRepository userProductMappingRepository;
    @Autowired
    private ProductRepository productRepository;

    public HashMap<String, Object> getAllUsers(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Pageable pageable;
        if (pageNo == null || pageSize == null) {
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        }
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserDto> userDtos = userPage.getContent().stream()
                .map(user -> {
                    UserDto userDto = new UserDto();
                    userDto.setId(user.getId());
                    userDto.setName(user.getName());
                    userDto.setEmail(user.getEmail());
                    userDto.setUsername(user.getUsername());
                    userDto.setPassword("");
                    userDto.setRoleId(user.getUserRoleMappings().stream().findFirst().get().getRole().getId());
                    if (user.getCustomer() != null && user.getCustomer().getId() != null) {
                        userDto.setCustomerId(user.getCustomer().getId());
                    } else {

                        userDto.setCustomerId(null);
                    }
                    userDto.setProductIds(user.getUserProductMappings().stream().map(userProductMapping -> userProductMapping
                            .getCustomerProductMapping().getProduct().getId()).collect(Collectors.toList()));
                    return userDto;
                }).collect(Collectors.toList());
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, userDtos);
        return map;
    }

    public HashMap<String, Object> updateUser(UserDto userDto) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<User> optionalUser = userRepository.findById(userDto.getId());
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            if ("ImsAdmin".equals(existingUser.getUsername())) {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "User 'ImsAdmin' is not editable");
                return map;
            }
            long existingZoneCount = userRepository.countByUsernameIgnoreCase(userDto.getUsername());
            if (existingZoneCount > 0) {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.USERNAME_ALREADY_EXIST);
                return map;
            } else {
                existingUser.setUsername(userDto.getUsername());
            }
            long existingEmailCount = userRepository.countByEmailIgnoreCase(userDto.getEmail());
            if (existingEmailCount > 0) {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.EMAIL_ALREADY_EXIST);
                return map;
            }
            else{
                existingUser.setEmail(userDto.getEmail());
            }
            existingUser.setName(userDto.getName());
            existingUser.setEmail(userDto.getEmail());
            Optional<Customer> optionalCustomer = customerRepository.findById(userDto.getCustomerId());
            if (optionalCustomer.isPresent()) {
                Customer customer = optionalCustomer.get();
                existingUser.setCustomer(customer);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
                return map;
            }
            Optional<Role> optionalRole = roleRepository.findById(userDto.getRoleId());
            if (optionalRole.isPresent()) {
                Role newRole = optionalRole.get();
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
            List<Integer> productIds = userDto.getProductIds();
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
            List<CustomerProductMapping> customerProductMappingList = customerProductMappingRepository.findByCustomerIdAndProductIdIn(userDto.getCustomerId(), userDto.getProductIds());
            Set<UserProductMapping> existingMappings = existingUser.getUserProductMappings();
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
                Set<Integer> existingCustomerProductIds = new HashSet<>();
                existingMappings.forEach(mapping -> existingCustomerProductIds.add(mapping.getCustomerProductMapping().getId()));
                Set<Integer> newCustomerProductIds = new HashSet<>(customerProductMappingList.stream().map(CustomerProductMapping::getId).collect(Collectors.toSet()));
                Set<Integer> customerProductIdsToRemove = new HashSet<>(existingCustomerProductIds);
                customerProductIdsToRemove.removeAll(newCustomerProductIds);
                existingMappings.removeIf(mapping -> customerProductIdsToRemove.contains(mapping.getCustomerProductMapping().getId()));
                Set<UserProductMapping> userProductMappings = new HashSet<>();
                for (Integer customerProductId : newCustomerProductIds) {
                    if (!existingCustomerProductIds.contains(customerProductId)) {
                        Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findById(customerProductId);
                        if (customerProductMappingOptional.isPresent()) {
                            CustomerProductMapping customerProductMapping = customerProductMappingOptional.get();
                            UserProductMapping newMapping = new UserProductMapping();
                            newMapping.setUser(existingUser);
                            newMapping.setCustomerProductMapping(customerProductMapping);
                            userProductMappings.add(newMapping);
                            userProductMappingRepository.save(newMapping);
                        }
                    }
                }

            } else {

                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "No Products are Associated for the CustomerId=" + userDto.getCustomerId());
                return map;
            }
            userRepository.save(existingUser);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
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


    public HashMap<String, Object> removeUser(Integer userId) {
        HashMap<String, Object> map = new HashMap<>();
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> registerUser(UserDto userDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = userRepository.countByUsernameIgnoreCase(userDto.getUsername());
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
        user.setUsername(userDto.getUsername());
        String newPass = passwordEncoder.encode(userDto.getPassword());
        user.setPassword(newPass);
        user.setLdapUser(false);
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        user.setCreatedTime(timestamp);
        user.setLoginTime(timestamp);
        User loggedInUser = utility.getLoggedInUser(token);
        user.setUser(loggedInUser);
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
//            admin -> admin (cus id =null and produ=null, role =admin)
//            admin -> user (cus id =3 and produ=[4,5], role =user)
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

        Set<UserProductMapping> userProductMappings = new HashSet<>();
        if (userDto.getCustomerId() != null && userDto.getProductIds() != null) {
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
                user.setUserProductMappings(userProductMappings);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "No Products are Associated for the CustomerId=" + userDto.getCustomerId());
                return map;
            }
        } else {
            user.setUserProductMappings(userProductMappings);
        }

        user.setUserProductMappings(userProductMappings);
        User savedUser = userRepository.save(user);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.REGISTERED_SUCCESS);
        map.put(Constant.DATA, savedUser.getId());
        return map;
    }

    @Override
    public HashMap<String, Object> getAllUserBasicDetails(String token, String searchByUsername, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> userList = new ArrayList<>();
        Set<User> usersOfUser = user.getUsers();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"username"}).ascending());
        Page<User> userPage = userRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!usersOfUser.isEmpty()) {
                predicates.add(root.in(usersOfUser));
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
            data.put("Username", user1.getUsername());
            userList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, userList);
        return map;
    }

}



