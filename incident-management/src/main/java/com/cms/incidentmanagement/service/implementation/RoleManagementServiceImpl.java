package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.FeatureDto;
import com.cms.incidentmanagement.dto.ProductDto;
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
    private RoleFeatureMappingRepository roleFeatureMappingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Utilities utility;


    @Override
    public HashMap<String, Object> getAllRoles(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Pageable pageable;
        if (pageNo == null || pageSize == null) {
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        }
        Page<Role> rolePage = roleRepository.findAll(pageable);
        List<RoleDto> roleDtos = rolePage.getContent().stream()
                .map(role -> {
                    RoleDto roleDto = new RoleDto();
                    roleDto.setId(role.getId());
                    roleDto.setName(role.getName());
                    roleDto.setCreatedBy(role.getUser().getId());
                    List<Integer> featureIds = roleFeatureMappingRepository.findByRoleId(role.getId())
                            .stream()
                            .map(mapping -> mapping.getFeature().getId())
                            .collect(Collectors.toList());
                    roleDto.setFeatureIds(featureIds);
                    return roleDto;
                })
                .collect(Collectors.toList());
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, roleDtos);
        return map;
    }

    @Override
    public HashMap<String, Object> createNewRole(RoleDto roleDto) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = roleRepository.countByNameIgnoreCase(roleDto.getName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DUPLICATE_ROLE_NAME);
            return map;
        }
            Role roleObj = new Role();
            roleObj.setName(roleDto.getName());
            roleObj.setUser(userRepository.findById(roleDto.getCreatedBy()).get());

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
            RoleDto obj = new RoleDto();
            obj.setId(createdNewRole.getId());
            obj.setName(createdNewRole.getName());
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.CREATE_SUCCESS);
            map.put(Constant.DATA, obj.getId());

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
    public HashMap<String, Object> updateRole(RoleDto roleDto) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Role> roleOptional = roleRepository.findById(roleDto.getId());
        if (roleOptional.isPresent()) {
            Role role = roleOptional.get();
            role.setName(roleDto.getName());
            role.setUser(userRepository.findById(roleDto.getCreatedBy()).get());
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
                        roleFeatureMappingRepository.save(newMapping);
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
    public HashMap<String, Object> getAllRolesBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> roleList = new ArrayList<>();
        Set<UserRoleMapping> userRoleMappings = user.getUserRoleMappings();
        Set<Role> userRoles= userRoleMappings.stream()
                .map(UserRoleMapping::getRole)
                .collect(Collectors.toSet());
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Role> rolePage = roleRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!userRoles.isEmpty()) {
                predicates.add(root.in(userRoles));
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
            roleList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, roleList);
        return map;


    }

    @Override
    public HashMap<String, Object> getRoles() {
        HashMap<String, Object> map = new HashMap<>();
        List<Role> roles = roleRepository.findAll();
        List<Map<String, Object>> roleDataList = new ArrayList<>();
        for (Role role : roles) {
            Map<String, Object> roleData = new HashMap<>();
            roleData.put("id", role.getId());
            roleData.put("roleName", role.getName());
            roleDataList.add(roleData);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, roleDataList);
        return map;
    }
}

