package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.entity.Area;
import com.cms.core.repository.AreaRepository;
import com.cms.core.repository.AreaUserMappingRepository;
import com.cms.core.repository.UserRepository;
import com.cms.core.repository.ZoneRepository;
import com.cms.incidentmanagement.dto.AreaDto;
import com.cms.incidentmanagement.service.AreaService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shashidhar on 5/14/2024.
 */
@Service
public class AreaServiceImpl implements AreaService {
    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Utilities utility;

    @Override
    public HashMap<String, Object> addArea(AreaDto areaDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Zone> optionalZone = zoneRepository.findById(areaDto.getZoneId());
        Zone zone = optionalZone.get();
        long existingAreaCount;
        if (zone != null) {
            existingAreaCount = areaRepository.countByNameIgnoreCaseAndZoneId(areaDto.getName(), areaDto.getZoneId());
        } else {
            existingAreaCount = areaRepository.countByNameIgnoreCaseAndZoneIdIsNull(areaDto.getName());
        }
        if (existingAreaCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.AREA_ALREADY_EXISTS);
            return map;
        }
        Area area = new Area();
        area.setName(areaDto.getName());
        area.setPolygon(areaDto.getPolygon());
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        area.setCreatedTime(timestamp);
        User loggedInUser = utility.getLoggedInUser(token);
        area.setUser(loggedInUser);
        if (optionalZone.isPresent()) {
            area.setZone(zone);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ZONE_NOT_FOUND);
            return map;
        }
        List<Integer> userIds = areaDto.getUserIds();
        Set<AreaUserMapping> areaUserMappings = new HashSet<>();
        for (Integer userId : userIds) {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                AreaUserMapping areaUserMapping = new AreaUserMapping();
                areaUserMapping.setUser(user);
                areaUserMapping.setArea(area);
                areaUserMappings.add(areaUserMapping);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND + "for userId=" + userId);
                return map;
            }
        }
        area.setAreaUserMappings(areaUserMappings);
        Area savedArea = areaRepository.save(area);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.ADD_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedArea.getId());
        }});
        return map;
    }

    @Override
    public HashMap<String, Object> getAllAreas(String token, Integer pageNo, Integer pageSize, String searchByName, Integer zoneId) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> areaList = new ArrayList<>();
        Zone zone = null;
        if (zoneId != null) {
            Optional<Zone> optionalZone = zoneRepository.findById(zoneId);
            if (optionalZone.isPresent()) {
                zone = optionalZone.get();
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.ZONE_NOT_FOUND);
                return map;
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Zone finalZone = zone;
        User user = utility.getLoggedInUser(token);
        Customer usersCustomer = user.getCustomer();
        Page<Area> areaPage = areaRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalZone != null) {
                predicates.add(criteriaBuilder.equal(root.get("zone"), finalZone));
            }
            if (usersCustomer != null) {
                Join<Area, Zone> areaZoneJoin = root.join("zone", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(areaZoneJoin.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        areaPage.stream().forEach(area -> {
            Set<AreaUserMapping> areaUserMappings = area.getAreaUserMappings();
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", area.getId());
            data.put("name", area.getName());
            data.put("zoneId", area.getZone().getId());
            if (!areaUserMappings.isEmpty()) {
                List<Map<String, Object>> user1 = areaUserMappings.stream().map(mapping -> mapping.getUser()).map(user2 -> {
                    Map<String, Object> userDetails = new HashMap<>();
                    userDetails.put("id", user2.getId());
                    userDetails.put("userName", user2.getUsername());
                    return userDetails;
                }).collect(Collectors.toList());
                data.put("User", user1);
            }
            data.put("customerId", area.getZone().getCustomer().getId());
            data.put("polygon", area.getPolygon());
            areaList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, areaList);
        return map;
    }

    @Override
    public HashMap<String, Object> updateArea(AreaDto areaDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Area> areaOptional = areaRepository.findById(areaDto.getId());
        if (areaOptional.isPresent()) {
            Area updatedArea = areaOptional.get();
            if (areaDto.getZoneId() == updatedArea.getZone().getId()) {
                updatedArea.setName(areaDto.getName());
                updatedArea.setPolygon(areaDto.getPolygon());
                Set<AreaUserMapping> existingMappings = updatedArea.getAreaUserMappings();
                Set<Integer> existingUserIds = new HashSet<>();
                existingMappings.forEach(mapping -> existingUserIds.add(mapping.getUser().getId()));
                Set<Integer> newUserIds = new HashSet<>(areaDto.getUserIds());
                Set<Integer> userIdsToRemove = new HashSet<>(existingUserIds);
                userIdsToRemove.removeAll(newUserIds);
                existingMappings.removeIf(mapping -> userIdsToRemove.contains(mapping.getUser().getId()));
                for (Integer userId : newUserIds) {
                    if (!existingUserIds.contains(userId)) {
                        Optional<User> userOptional = userRepository.findById(userId);
                        if (userOptional.isPresent()) {
                            User user = userOptional.get();
                            AreaUserMapping newMapping = new AreaUserMapping();
                            newMapping.setArea(updatedArea);
                            newMapping.setUser(user);
                            updatedArea.getAreaUserMappings().add(newMapping);
                        } else {
                            map.put(Constant.STATUS, Constant.ERROR);
                            map.put(Constant.MESSAGE, "User not found for ID: " + userId);
                            return map;
                        }
                    }
                }
                areaRepository.save(updatedArea);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "Cannot change the area from one zone to another");
            }

        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
        }

        return map;
    }

    @Override
    public HashMap<String, Object> removeArea(Integer areaId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Area> optionalArea = areaRepository.findById(areaId);
        if (optionalArea.isPresent()) {
            Area area = optionalArea.get();
            area.setUser(null);
            areaRepository.deleteById(areaId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DEVICE_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getAllAreasBasicDetails(String searchByName, Integer pageNo, Integer pageSize, Integer zoneId, String token) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> areaList = new ArrayList<>();
        Zone zone = null;
        if (zoneId != null) {
            Optional<Zone> optionalZone = zoneRepository.findById(zoneId);
            if (optionalZone.isPresent()) {
                zone = optionalZone.get();
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.ZONE_NOT_FOUND);
                return map;
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Zone finalZone = zone;
        User user = utility.getLoggedInUser(token);
        Customer usersCustomer = user.getCustomer();
        Page<Area> areaPage = areaRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalZone != null) {
                predicates.add(criteriaBuilder.equal(root.get("zone"), finalZone));
            }
            if (usersCustomer != null) {
                Join<Area, Zone> areaZoneJoin = root.join("zones", JoinType.INNER);
                Join<Zone, Customer> customerZoneJoin = areaZoneJoin.join("zone", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(customerZoneJoin.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        areaPage.stream().forEach(area -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", area.getId());
            data.put("name", area.getName());
            areaList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, areaList);
        return map;
    }
}
