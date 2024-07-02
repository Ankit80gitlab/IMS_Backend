package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.AreaRepository;
import com.cms.core.repository.CustomerRepository;
import com.cms.core.repository.ZoneRepository;
import com.cms.incidentmanagement.dto.ZoneDto;
import com.cms.incidentmanagement.service.ZoneService;
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
 * Created by Shashidhar on 5/13/2024.
 */
@Service
public class ZoneServiceImpl implements ZoneService {
    @Autowired
    private ZoneRepository zoneRepository;
    @Autowired
    private Utilities utility;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AreaRepository areaRepository;

    @Override
    public HashMap<String, Object> addZone(ZoneDto zoneDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = zoneRepository.countByNameIgnoreCase(zoneDto.getName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ZONE_ALREADY_EXISTS);
            return map;
        }

        Zone zone = new Zone();
        zone.setName(zoneDto.getName().trim());
        zone.setPolygon(zoneDto.getPolygon());
        Date date = new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        zone.setCreatedTime(timestamp);
        User loggedInUser = utility.getLoggedInUser(token);
        zone.setUser(loggedInUser);
        Optional<Customer> optionalCustomer = customerRepository.findById(zoneDto.getCustomerId());
        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();
            zone.setCustomer(customer);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
            return map;
        }
        Zone savedZone = zoneRepository.save(zone);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.ADD_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedZone.getId());
        }});

        return map;
    }

    @Override
    public HashMap<String, Object> getAllZones(Integer pageNo, Integer pageSize, String token, String searchByName) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> zoneList = new ArrayList<>();
        Customer usersCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Zone> zonePage = zoneRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (usersCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        zonePage.stream().forEach(zone -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", zone.getId());
            data.put("name", zone.getName());
            data.put("polygon", zone.getPolygon());
            data.put("customer", new HashMap<String, Object>() {{
                Customer customer = zone.getCustomer();
                put("id", customer.getId());
                put("name", customer.getName());
            }});
            zoneList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, zoneList);
        return map;

    }

    @Override
    public HashMap<String, Object> updateZone(ZoneDto zoneDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Zone> zoneOptional = zoneRepository.findById(zoneDto.getId());
        if (zoneOptional.isPresent()) {
            Zone updatedZone = zoneOptional.get();
            updatedZone.setName(zoneDto.getName().trim());
            updatedZone.setPolygon(zoneDto.getPolygon());
            User loggedInUser = utility.getLoggedInUser(token);
            updatedZone.setUser(loggedInUser);
            Optional<Customer> optionalCustomer = customerRepository.findById(zoneDto.getCustomerId());
            if (optionalCustomer.isPresent()) {
                Optional<Zone> zone = zoneRepository.findByCustomerId(zoneDto.getCustomerId());
                if (zone.isPresent()) {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "Customer is already associated with zone");
                    return map;
                } else {
                    Customer customer = optionalCustomer.get();
                    updatedZone.setCustomer(customer);
                }
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
                return map;
            }

            zoneRepository.save(updatedZone);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND);
        }

        return map;
    }

    @Override
    public HashMap<String, Object> removeZone(Integer zoneId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Zone> optionalZone = zoneRepository.findById(zoneId);
        if (optionalZone.isPresent()) {
            Zone zone = optionalZone.get();
            zone.setUser(null);
            zoneRepository.deleteById(zoneId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.ZONE_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getZoneInformations(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Pageable pageable;
        if (pageNo == null || pageSize == null) {
            pageable = Pageable.unpaged();
        } else {
            pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        }
        Page<Zone> zonePage = zoneRepository.findAll(pageable);
        List<Map<String, Object>> zoneList = new ArrayList<>();
        for (Zone zone : zonePage) {
            Map<String, Object> zoneData = new HashMap<>();
            zoneData.put("zoneid", zone.getId());
            zoneData.put("zonename", zone.getName());
            zoneData.put("zonepolygon", zone.getPolygon());
            List<Area> areas = areaRepository.findByZoneId(zone.getId());
            List<Map<String, Object>> areaList = new ArrayList<>();
            if (areas != null) {
                for (Area area : areas) {
                    Map<String, Object> areaData = new HashMap<>();
                    areaData.put("areaId", area.getId());
                    areaData.put("areaName", area.getName());
                    areaData.put("areaPolygon", area.getPolygon());
                    List<Map<String, Object>> devices = area.getAreaDeviceMappings().stream()
                            .map(mapping -> {
                                Device device = mapping.getDevice();
                                Map<String, Object> deviceData = new HashMap<>();
                                deviceData.put("id", device.getId());
                                deviceData.put("name", device.getName());
                                deviceData.put("type", device.getDescription());
                                return deviceData;
                            })
                            .collect(Collectors.toList());

                    areaData.put("devices", devices);
                    areaList.add(areaData);
                }
                zoneData.put("zoneareas", areaList);
            } else {
                zoneData.put("zoneAreas", areaList);
            }

            zoneList.add(zoneData);
        }

        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, zoneList);
        return map;
    }

    @Override
    public HashMap<String, Object> getZoneAreaInformations(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        List<Zone> zones = zoneRepository.findAll();
        List<Map<String, Object>> zoneDataList = new ArrayList<>();
        for (Zone zone : zones) {
            Map<String, Object> zoneData = new HashMap<>();
            zoneData.put("id", zone.getId());
            zoneData.put("zoneName", zone.getName());
            List<Area> areas = areaRepository.findByZoneId(zone.getId());
            List<Map<String, Object>> areaList = new ArrayList<>();
            if (areas != null) {
                for (Area area : areas) {
                    Map<String, Object> areaData = new HashMap<>();
                    areaData.put("areaId", area.getId());
                    areaData.put("areaName", area.getName());
                    areaList.add(areaData);
                }
                zoneData.put("zoneareas", areaList);
            } else {
                zoneData.put("zoneAreas", areaList);
            }

            zoneDataList.add(zoneData);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, zoneDataList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllZonesBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> zoneList = new ArrayList<>();
        Customer usersCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Zone> zonePage = zoneRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (usersCustomer != null) {
                predicates.add(criteriaBuilder.equal(root.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        zonePage.stream().forEach(zone -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", zone.getId());
            data.put("name", zone.getName());
            zoneList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, zoneList);
        return map;
    }
}

