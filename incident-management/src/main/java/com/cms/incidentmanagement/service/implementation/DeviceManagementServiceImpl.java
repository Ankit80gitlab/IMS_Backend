package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.DeviceDto;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.service.DeviceManagementService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shashidhar on 5/13/2024.
 */
@Service
public class DeviceManagementServiceImpl implements DeviceManagementService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private Utilities utility;

    @Autowired
    private CustomerProductMappingRepository customerProductMappingRepository;

    @Autowired
    private AreaRepository areaRepository;
    @Autowired
    private CustomerProductMappingDeviceRepository customerProductMappingDeviceRepository;


    @Override
    public HashMap<String, Object> addDevice(DeviceDto deviceDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = deviceRepository.countByNameIgnoreCase(deviceDto.getName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DEVICE_ALREADY_EXISTS);
            return map;
        }
           Device device = new Device();
            device.setName(deviceDto.getName());
            device.setLat(deviceDto.getLat());
            device.setLon(deviceDto.getLon());
            device.setDescription(deviceDto.getDescription());
            User loggedInUser = utility.getLoggedInUser(token);
            device.setUser(loggedInUser);
            Optional<CustomerProductMapping> optionalCustomerProductMapping=customerProductMappingRepository.findById(deviceDto.getCustomerProductMappingId());
            if(optionalCustomerProductMapping.isPresent()){
                CustomerProductMapping customerProductMapping=optionalCustomerProductMapping.get();
                Set<CustomerProductMappingDevice> customerProductMappingDevices = new HashSet<>();
                CustomerProductMappingDevice customerProductMappingDevice=new CustomerProductMappingDevice();
                customerProductMappingDevice.setDevice(device);
                customerProductMappingDevice.setCustomerProductMapping(customerProductMapping);
                customerProductMappingDevices.add(customerProductMappingDevice);
                device.setCustomerProductMappingDevices(customerProductMappingDevices);
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.CUSTOMER_PRODUCT_MAPPING_NOT_FOUND);
                return map;
            }
           Optional<Area>optionalArea= areaRepository.findById(deviceDto.getAreaId());
            if(optionalArea.isPresent()){
                Area area=optionalArea.get();
                Set<AreaDeviceMapping> areaDeviceMappings = new HashSet<>();
                AreaDeviceMapping areaDeviceMapping=new AreaDeviceMapping();
                areaDeviceMapping.setDevice(device);
                areaDeviceMapping.setArea(area);
                areaDeviceMappings.add(areaDeviceMapping);
                device.setAreaDeviceMappings(areaDeviceMappings);
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
                return map;
            }
            Device savedDevice =deviceRepository.save(device);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.REGISTERED_SUCCESS);
            map.put(Constant.DATA, savedDevice.getId());

        return map;
    }

    @Override
    public HashMap<String, Object> getAllDevices(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Page<Device> devices;
        if (pageNo == null) {
            devices = deviceRepository.findAll(Pageable.unpaged());
        } else {
            devices = deviceRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("id").descending()));
        }
        List<DeviceDto> deviceDtoList = new ArrayList<>();
        for (Device device : devices) {
            DeviceDto dto = new DeviceDto();
            dto.setId(device.getId());
            dto.setName(device.getName());
            dto.setDescription(device.getDescription());
            dto.setLon(device.getLon());
            dto.setLat(device.getLat());
            dto.setCustomerProductMappingId(device.getCustomerProductMappingDevices().stream().findFirst().get().getCustomerProductMapping().getId());
            dto.setAreaId(device.getAreaDeviceMappings().stream().findFirst().get().getArea().getId());
            deviceDtoList.add(dto);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, deviceDtoList);
        return map;
    }
    @Transactional
    @Override
    public HashMap<String, Object> updateDevice(DeviceDto deviceDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Device> deviceOptional = deviceRepository.findById(deviceDto.getId());
        if (deviceOptional.isPresent()) {
            Device updatedDevice = deviceOptional.get();
            updatedDevice.setName(deviceDto.getName());
            updatedDevice.setDescription(deviceDto.getDescription());
            updatedDevice.setLat(deviceDto.getLat());
            updatedDevice.setLon(deviceDto.getLon());
            User loggedInUser = utility.getLoggedInUser(token);
            updatedDevice.setUser(loggedInUser);
//            Optional<CustomerProductMapping> optionalCustomerProductMapping=customerProductMappingRepository.findById(deviceDto.getCustomerProductMappingId());
//            if(optionalCustomerProductMapping.isPresent()){
//                CustomerProductMapping customerProductMapping=optionalCustomerProductMapping.get();
//                Optional<CustomerProductMappingDevice> existingMapping = updatedDevice.getCustomerProductMappingDevices().stream()
//                        .filter(mapping -> mapping.getCustomerProductMapping().getId().equals(customerProductMapping.getId()))
//                        .findFirst();
//
//                if (existingMapping.isPresent()) {
//                    updatedDevice.getCustomerProductMappingDevices().clear();
//                    CustomerProductMappingDevice customerProductMappingDevice = existingMapping.get();
//                    customerProductMappingDevice.setCustomerProductMapping(customerProductMapping);
//                } else {
//                    CustomerProductMappingDevice customerProductMappingDevice = new CustomerProductMappingDevice();
//                    customerProductMappingDevice.setDevice(updatedDevice);
//                    customerProductMappingDevice.setCustomerProductMapping(customerProductMapping);
//                    updatedDevice.getCustomerProductMappingDevices().add(customerProductMappingDevice);
//                }
//            }
//            else {
//                map.put(Constant.STATUS, Constant.ERROR);
//                map.put(Constant.MESSAGE, Constant.CUSTOMER_PRODUCT_MAPPING_NOT_FOUND);
//                return map;
//            }

            Set<CustomerProductMappingDevice> existingMappings = updatedDevice.getCustomerProductMappingDevices();
            Set<Integer> existingCustomerProductMappingIds = new HashSet<>();
            existingMappings.forEach(mapping -> existingCustomerProductMappingIds.add(mapping.getCustomerProductMapping().getId()));
            Set<Integer> newCustomerProductMappingId = new HashSet<>(deviceDto.getCustomerProductMappingId());
            Set<Integer> customerProductMappingIdsToRemove = new HashSet<>(existingCustomerProductMappingIds);
            customerProductMappingIdsToRemove .removeAll(newCustomerProductMappingId);
            existingMappings.removeIf(mapping -> customerProductMappingIdsToRemove.contains(mapping.getCustomerProductMapping().getId()));
            Set<CustomerProductMappingDevice> customerProductMappingDevices = new HashSet<>();
            for (Integer customerProductMappingId: newCustomerProductMappingId) {
                if (!existingCustomerProductMappingIds.contains( customerProductMappingId)) {
                    Optional<CustomerProductMapping> OptionalCustomerProductMapping = customerProductMappingRepository.findById(customerProductMappingId);
                    if (OptionalCustomerProductMapping.isPresent()) {
                        CustomerProductMapping customerProductMapping  = OptionalCustomerProductMapping.get();
                        CustomerProductMappingDevice newMapping = new CustomerProductMappingDevice();
                        newMapping.setCustomerProductMapping(customerProductMapping);
                        newMapping.setDevice(updatedDevice);
                       customerProductMappingDevices.add(newMapping);
                        updatedDevice.setCustomerProductMappingDevices(customerProductMappingDevices);
                    } else {
                        map.put(Constant.STATUS, Constant.ERROR);
                        map.put(Constant.MESSAGE, "CustomerProductMapping not found for ID: " + customerProductMappingId);
                        return map;

                    }
                }
            }

            Optional<Area> optionalArea=areaRepository.findById(deviceDto.getAreaId());
            if(optionalArea.isPresent()){
                Area area=optionalArea.get();
                Optional<AreaDeviceMapping> existingMapping = updatedDevice.getAreaDeviceMappings().stream()
                        .filter(mapping -> mapping.getArea().getId().equals(area.getId()))
                        .findFirst();

                if (existingMapping.isPresent()) {

                    AreaDeviceMapping areaDeviceMapping = existingMapping.get();
                    areaDeviceMapping.setArea(area);
                } else {

                    AreaDeviceMapping newAreaDeviceMapping = new AreaDeviceMapping();
                    newAreaDeviceMapping.setArea(area);
                    newAreaDeviceMapping.setDevice(updatedDevice);
                    updatedDevice.getAreaDeviceMappings().add(newAreaDeviceMapping);
                }
            }
            else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
                return map;
            }
            deviceRepository.save(updatedDevice);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DEVICE_NOT_FOUND);
        }

        return map;
    }

    @Override
    public HashMap<String, Object> removeDevice(Integer deviceId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Device> optionalDevice= deviceRepository.findById(deviceId);
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            device.setUser(null);
            deviceRepository.deleteById(deviceId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DEVICE_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getCustomerProductMappings() {
        HashMap<String, Object> map = new HashMap<>();
        List<CustomerProductMapping> customerProductMappings = customerProductMappingRepository.findAll();
        List<Map<String, Object>> customerProductMappingDataList = new ArrayList<>();
        for (CustomerProductMapping customerProductMapping : customerProductMappings) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", customerProductMapping.getId());
            productData.put("customerName", customerProductMapping.getCustomer().getName());
            productData.put("productName", customerProductMapping.getProduct().getName());
            customerProductMappingDataList.add(productData);
        }

        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, customerProductMappingDataList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllDevicesBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> deviceList = new ArrayList<>();
        Set<Device> userDevices = user.getDevices();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Device> devicePage = deviceRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!userDevices.isEmpty()) {
                predicates.add(root.in(userDevices));
            }
            if(searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
        }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        devicePage.stream().forEach(device -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", device.getId());
            data.put("name", device.getName());
            deviceList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, deviceList);
        return map;
    }
    }


