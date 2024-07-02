package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.FeatureDto;
import com.cms.incidentmanagement.dto.RoleDto;
import com.cms.incidentmanagement.service.RoleManagementService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleManagementServiceImpl implements RoleManagementService {
    @Autowired
    private UserRoleMappingRepository userRoleMappingRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private FeatureRepository featureRepository;
    @Autowired
    private Utilities utility;
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public HashMap<String, Object> getAllRoles(Integer pageNo, Integer pageSize, String token, String searchByName) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> roleList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Role> rolePage = roleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), userCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        rolePage.stream().forEach(role -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", role.getId());
            data.put("name", role.getName());
            data.put("createdBy", role.getUser().getId());
            List<Integer> featureIds = role.getRoleFeatureMappings().stream().map(mapping -> mapping.getFeature().getId()).collect(Collectors.toList());
            data.put("featureIds", featureIds);
            roleList.add(data);

        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, roleList);
        return map;
    }

    @Override
    public HashMap<String, Object> createNewRole(RoleDto roleDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        String roleName = roleDto.getName();
        User user = utility.getLoggedInUser(token);
        Customer customer = user.getCustomer();
        long existingRoleCount;
        if (customer != null) {
            existingRoleCount = roleRepository.countByNameIgnoreCaseAndCustomerId(roleName, customer.getId());
        } else {
            existingRoleCount = roleRepository.countByNameIgnoreCaseAndCustomerIdIsNull(roleName);
        }

        if (existingRoleCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DUPLICATE_ROLE_NAME);
            return map;
        }
        Role roleObj = new Role();
        roleObj.setName(roleDto.getName().trim());
        roleObj.setUser(user);
        if (customer == null) {
            roleObj.setCustomer(null);
        } else {
            Optional<Customer> customerOptional = customerRepository.findById(customer.getId());
            if (customerOptional.isPresent()) {
                Customer customer1 = customerOptional.get();
                roleObj.setCustomer(customer1);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
                return map;
            }
        }

        List<Integer> featureIds = roleDto.getFeatureIds();
        List<Feature> assignedFeatures = featureIds.stream()
                .map(featureRepository::getById)
                .collect(Collectors.toList());
        Set<RoleFeatureMapping> roleFeatureMappings = new HashSet<>();
        assignedFeatures.forEach(feature -> {
            RoleFeatureMapping roleFeatureMapping = new RoleFeatureMapping();
            roleFeatureMapping.setFeature(feature);
            roleFeatureMapping.setRole(roleObj);
            roleFeatureMappings.add(roleFeatureMapping);
        });
        roleObj.setRoleFeatureMappings(roleFeatureMappings);
        Role createdNewRole = roleRepository.save(roleObj);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.CREATE_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", createdNewRole.getId());
        }});


        return map;
    }

    @Override
    public HashMap<String, Object> deleteRole(int roleId) {
        HashMap<String, Object> map = new HashMap<>();
        if (roleRepository.findById(roleId).isPresent()) {
            List<UserRoleMapping> userRoleMappingList = userRoleMappingRepository.findByRoleId(roleId);
            boolean userMappedWithRole = false;
            for (UserRoleMapping f : userRoleMappingList) {
                if (f.getRole().getId() == roleId) {
                    userMappedWithRole = true;
                    break;
                }
            }
            if (!userMappedWithRole) {
                roleRepository.deleteById(roleId);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.ROLE_ASSIGNED_TO_USER);
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ROLE_ID_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> updateRole(RoleDto roleDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Role> roleOptional = roleRepository.findById(roleDto.getId());
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            if ("ROLE_ADMIN".equals(role.getName())) {
                roleDto.setIsEditable(false);
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "Role 'ROLE_ADMIN' is not editable");
                return map;
            } else {
                String roleName = roleDto.getName().trim();
                User user = utility.getLoggedInUser(token);
                Customer customer = user.getCustomer();
                if (!role.getName().equalsIgnoreCase(roleName)) {
                    long existingRoleCount;
                    if (customer != null) {
                        existingRoleCount = roleRepository.countByNameIgnoreCaseAndCustomerId(roleName, customer.getId());
                    } else {
                        existingRoleCount = roleRepository.countByNameIgnoreCaseAndCustomerIdIsNull(roleName);
                    }
                    if (existingRoleCount > 0) {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, Constant.DUPLICATE_ROLE_NAME);
                        return map;
                    }
                    role.setName(roleDto.getName().trim());
                }
                Set<RoleFeatureMapping> existingMappings = role.getRoleFeatureMappings();
                Set<Integer> existingFeatureIds = new HashSet<>();
                existingMappings.forEach(mapping -> existingFeatureIds.add(mapping.getFeature().getId()));
                Set<Integer> newFeatureIds = new HashSet<>(roleDto.getFeatureIds());
                Set<Integer> featureIdsToRemove = new HashSet<>(existingFeatureIds);
                featureIdsToRemove.removeAll(newFeatureIds);
                existingMappings.removeIf(mapping -> featureIdsToRemove.contains(mapping.getFeature().getId()));
                for (Integer featureId : newFeatureIds) {
                    if (!existingFeatureIds.contains(featureId)) {
                        Optional<Feature> featureOptional = featureRepository.findById(featureId);
                        if (featureOptional.isPresent()) {
                            Feature feature = featureOptional.get();
                            RoleFeatureMapping newMapping = new RoleFeatureMapping();
                            newMapping.setFeature(feature);
                            newMapping.setRole(role);
                            role.getRoleFeatureMappings().add(newMapping);
                        } else {
                            map.put(Constant.STATUS, Constant.ERROR);
                            map.put(Constant.MESSAGE, "Feature not found for ID: " + featureId);
                            return map;
                        }
                    }
                }
                roleRepository.save(role);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
            }

        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ROLE_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getAllFeatures(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Page<Feature> features;
        if (pageNo == null) {
            features = featureRepository.findAll(Pageable.unpaged());
        } else {
            features = featureRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("id").descending()));
        }
        List<FeatureDto> featureDtoList = new ArrayList<>();
        for (Feature feature : features) {
            FeatureDto dto = new FeatureDto();
            dto.setId(feature.getId());
            dto.setName(feature.getName());
            dto.setPath(feature.getPath());
            featureDtoList.add(dto);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, featureDtoList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllRolesBasicDetails(String token) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> roleList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        List<Role> rolePage = roleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), userCustomer));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
        rolePage.stream().forEach(role -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", role.getId());
            data.put("name", role.getName());
            roleList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, roleList);
        return map;
    }

}

