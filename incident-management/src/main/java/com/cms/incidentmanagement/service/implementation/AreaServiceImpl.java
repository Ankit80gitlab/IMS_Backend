package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.AreaRepository;
import com.cms.core.repository.UserRepository;
import com.cms.core.repository.ZoneRepository;
import com.cms.incidentmanagement.dto.AreaDto;
import com.cms.incidentmanagement.dto.DeviceDto;
import com.cms.incidentmanagement.service.AreaService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
        long existingZoneCount = areaRepository.countByNameIgnoreCase(areaDto.getName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ZONE_ALREADY_EXISTS);
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
            Optional<Zone> optionalZone=zoneRepository.findById(areaDto.getZoneId());
            if(optionalZone.isPresent()){
                Zone zone=optionalZone.get();
                area.setZone(zone);
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.ZONE_NOT_FOUND);
                return map;
            }
            Optional<User>optionalUser= userRepository.findById(areaDto.getUserId());
            if(optionalUser.isPresent()){
                User user=optionalUser.get();
                Set<AreaUserMapping> areaUserMappings = new HashSet<>();
                AreaUserMapping areaUserMapping=new AreaUserMapping();
                areaUserMapping.setArea(area);
                areaUserMapping.setUser(user);
                areaUserMappings.add(areaUserMapping);
                area.setAreaUserMappings(areaUserMappings);
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
                return map;
            }
            Area savedArea =areaRepository.save(area);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.REGISTERED_SUCCESS);
            map.put(Constant.DATA, savedArea.getId());

        return map;
    }

    @Override
    public HashMap<String, Object> getAllAreas(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Page<Area> areas;
        if (pageNo == null) {
            areas = areaRepository.findAll(Pageable.unpaged());
        } else {
            areas = areaRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("id").descending()));
        }
        List<AreaDto> areaDtoList = new ArrayList<>();
        for (Area area : areas) {
            AreaDto dto = new AreaDto();
            dto.setId(area.getId());
            dto.setName(area.getName());
            dto.setZoneId(area.getZone().getId());
            dto.setPolygon(area.getPolygon());
            dto.setUserId(area.getAreaUserMappings().stream().findFirst().get().getUser().getId());
            areaDtoList.add(dto);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, areaDtoList);
        return map;
    }

    @Override
    public HashMap<String, Object> updateArea(AreaDto areaDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Area> areaOptional = areaRepository.findById(areaDto.getId());
        if (areaOptional.isPresent()) {
            Area updatedArea = areaOptional.get();
            updatedArea.setName(areaDto.getName());
            updatedArea.setPolygon(areaDto.getPolygon());
            User loggedInUser = utility.getLoggedInUser(token);
            updatedArea.setUser(loggedInUser);
            Optional<Zone> optionalZone=zoneRepository.findById(areaDto.getZoneId());
            if(optionalZone.isPresent()){
                Zone zone=optionalZone.get();
                updatedArea.setZone(zone);
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.ZONE_NOT_FOUND);
                return map;
            }

            Optional<User> optionalUser=userRepository.findById(areaDto.getUserId());
            if(optionalUser.isPresent()){
                User user=optionalUser.get();
                Optional<AreaUserMapping> existingMapping = updatedArea.getAreaUserMappings().stream()
                        .filter(mapping -> mapping.getUser().getId().equals(user.getId()))
                        .findFirst();

                if (existingMapping.isPresent()) {
                    AreaUserMapping areaUserMapping = existingMapping.get();
                    areaUserMapping.setUser(user);
                } else {

                    AreaUserMapping newUserRoleMapping = new AreaUserMapping();
                    newUserRoleMapping.setUser(user);
                    newUserRoleMapping.setArea(updatedArea);
                    updatedArea.getAreaUserMappings().add(newUserRoleMapping);
                }
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.USER_NOT_FOUND);
                return map;
            }

            areaRepository.save(updatedArea);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
        }

        return map;
    }

    @Override
    public HashMap<String, Object> removeArea(Integer areaId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Area> optionalArea= areaRepository.findById(areaId);
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
    public HashMap<String, Object> getAllAreasBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize) {

            HashMap<String, Object> map = new HashMap<>();
            User user = utility.getLoggedInUser(token);
            List<HashMap<String, Object>> areaList = new ArrayList<>();
        Set<AreaUserMapping> areaUserMappings = user.getAreaUserMappings();
        Set<Area> userAreas = areaUserMappings.stream()
                .map(AreaUserMapping::getArea)
                .collect(Collectors.toSet());
            PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
            Page<Area> areaPage = areaRepository.findAll((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                 if (!userAreas.isEmpty()) {
                    predicates.add(root.in(userAreas));
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
