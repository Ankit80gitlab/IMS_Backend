package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.DeviceDto;
import com.cms.incidentmanagement.service.DeviceManagementService;
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
import javax.transaction.Transactional;
import java.util.*;

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
    private ProductRepository productRepository;
    @Autowired
    private CustomerProductMappingDeviceRepository customerProductMappingDeviceRepository;
    @Autowired
    private AreaDeviceMappingRepository areaDeviceMappingRepository;

    @Transactional
    @Override
    public HashMap<String, Object> addDevice(DeviceDto deviceDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = deviceRepository.countByUidIgnoreCase(deviceDto.getUid());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DEVICE_ALREADY_EXISTS);
            return map;
        }
        Device device = new Device();
        device.setName(deviceDto.getName().trim());
        device.setUid(deviceDto.getUid());
        device.setLat(deviceDto.getLat());
        device.setLon(deviceDto.getLon());
        device.setDescription(deviceDto.getDescription());
        User loggedInUser = utility.getLoggedInUser(token);
        device.setUser(loggedInUser);
        Optional<Area> optionalArea = areaRepository.findById(deviceDto.getAreaId());
        Optional<Product> optionalProduct = productRepository.findById(deviceDto.getProductId());
        if (optionalArea.isPresent()) {
            Area area = optionalArea.get();
            Product product = optionalProduct.get();
            Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findOne((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                Join<CustomerProductMapping, Customer> customerProductMappingZoneJoin = root.join("customer");
                Join<Customer, Zone> zoneJoin = customerProductMappingZoneJoin.join("zones");
                Join<Zone, Area> areaJoin = zoneJoin.join("areas");
                predicates.add(criteriaBuilder.equal(areaJoin, area));
                predicates.add(criteriaBuilder.equal(root.get("product"), product));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            });
            if (customerProductMappingOptional.isPresent()) {
                CustomerProductMappingDevice customerProductMappingDevice = new CustomerProductMappingDevice();
                customerProductMappingDevice.setDevice(device);
                customerProductMappingDevice.setCustomerProductMapping(customerProductMappingOptional.get());
                device.setCustomerProductMappingDevice(customerProductMappingDevice);
                AreaDeviceMapping areaDeviceMapping = new AreaDeviceMapping();
                areaDeviceMapping.setDevice(device);
                areaDeviceMapping.setArea(area);
                device.setAreaDeviceMapping(areaDeviceMapping);
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.CUSTOMER_PRODUCT_MAPPING_NOT_FOUND);
                return map;
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
            return map;
        }
        Device savedDevice = deviceRepository.save(device);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.ADD_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedDevice.getId());
        }});

        return map;
    }

    @Override
    public HashMap<String, Object> getAllDevices(Integer pageNo, Integer pageSize, String searchByName, Integer areaId, String token) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> deviceList = new ArrayList<>();
        Area area = null;
        if (areaId != null) {
            Optional<Area> areaOptional = areaRepository.findById(areaId);
            if (areaOptional.isPresent()) {
                area = areaOptional.get();
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
                return map;
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Area finalArea = area;
        User user = utility.getLoggedInUser(token);
        Customer usersCustomer = user.getCustomer();
        Page<Device> devicePage = deviceRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalArea != null) {
                Join<Device, AreaDeviceMapping> areaDeviceMappingJoin = root.join("areaDeviceMapping", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(areaDeviceMappingJoin.get("area"), finalArea));
            }
            if (usersCustomer != null) {
                Join<Device, CustomerProductMappingDevice> deviceCustomerProductMappingDeviceJoin = root.join("customerProductMappingDevice", JoinType.INNER);
                Join<CustomerProductMappingDevice, CustomerProductMapping> customerProductMappingDeviceCustomerProductMappingJoin = deviceCustomerProductMappingDeviceJoin.join("customerProductMapping", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(customerProductMappingDeviceCustomerProductMappingJoin.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        devicePage.stream().forEach(device -> {
            CustomerProductMapping customerProductMapping = device.getCustomerProductMappingDevice().getCustomerProductMapping();
            Product product = customerProductMapping.getProduct();
            AreaDeviceMapping areaDeviceMapping = device.getAreaDeviceMapping();
            Integer areaId1 = null;
            Integer zoneId = null;
            if (areaDeviceMapping != null) {
                Area area1 = areaDeviceMapping.getArea();
                areaId1 = area1.getId();
                zoneId = area1.getZone().getId();
            }
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", device.getId());
            data.put("name", device.getName());
            data.put("uid", device.getUid());
            data.put("lat", device.getLat());
            data.put("lon", device.getLon());
            data.put("description", device.getDescription());
            data.put("areaId", areaId1);
            data.put("zoneId", zoneId);
            data.put("customerId", customerProductMapping.getCustomer().getId());
            data.put("product", new HashMap<String, Object>() {{
                put("id", product.getId());
                put("name", product.getName());
            }});
            deviceList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, deviceList);
        return map;
    }

    @Override
    public HashMap<String, Object> updateDevice(DeviceDto deviceDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Device> deviceOptional = deviceRepository.findById(deviceDto.getId());
        if (deviceOptional.isPresent()) {
            Device updatedDevice = deviceOptional.get();
            if (updatedDevice.getName().equalsIgnoreCase(deviceDto.getName())) {
                updatedDevice.setName(deviceDto.getName());
            } else {
                long existingZoneCount = deviceRepository.countByUidIgnoreCase(deviceDto.getUid());
                if (existingZoneCount > 0) {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, Constant.DEVICE_ALREADY_EXISTS);
                    return map;
                }
            }
            updatedDevice.setUid(deviceDto.getName());
            updatedDevice.setName(deviceDto.getName().trim());
            updatedDevice.setDescription(deviceDto.getDescription());
            updatedDevice.setLat(deviceDto.getLat());
            updatedDevice.setLon(deviceDto.getLon());
            Optional<Area> optionalArea = areaRepository.findById(deviceDto.getAreaId());
            Optional<Product> optionalProduct = productRepository.findById(deviceDto.getProductId());
            if (optionalArea.isPresent()) {
                Area area = optionalArea.get();
                Optional<CustomerProductMapping> customerProductMappingOptional = customerProductMappingRepository.findOne((root, query, criteriaBuilder) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    Join<CustomerProductMapping, Customer> customerProductMappingZoneJoin = root.join("customer");
                    Join<Customer, Zone> zoneJoin = customerProductMappingZoneJoin.join("zones");
                    Join<Zone, Area> areaJoin = zoneJoin.join("areas");
                    predicates.add(criteriaBuilder.equal(areaJoin, area));
                    predicates.add(criteriaBuilder.equal(root.get("product"), optionalProduct.get()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                });
                if (customerProductMappingOptional.isPresent()) {
                    Optional<CustomerProductMappingDevice> customerProductMappingDeviceOptional = customerProductMappingDeviceRepository.findByDeviceId(deviceDto.getId());
                    if (customerProductMappingDeviceOptional.isPresent()) {
                        CustomerProductMappingDevice existingCustomerProductMappingDevice = customerProductMappingDeviceOptional.get();
                        existingCustomerProductMappingDevice.setDevice(updatedDevice);
                        existingCustomerProductMappingDevice.setCustomerProductMapping(customerProductMappingOptional.get());
                        updatedDevice.setCustomerProductMappingDevice(existingCustomerProductMappingDevice);
                    }
                    Optional<AreaDeviceMapping> areaDeviceMappingOptional = areaDeviceMappingRepository.findByDeviceId(deviceDto.getId());
                    if (areaDeviceMappingOptional.isPresent()) {
                        AreaDeviceMapping existingAreaDeviceMapping = areaDeviceMappingOptional.get();
                        existingAreaDeviceMapping.setDevice(updatedDevice);
                        existingAreaDeviceMapping.setArea(area);
                        updatedDevice.setAreaDeviceMapping(existingAreaDeviceMapping);
                    }
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, "The customer does not have any products assigned to their account .Please ensure the customer has an associated product before adding a device");
                    return map;
                }
            } else {
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
        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            Set<Ticket> tickets = device.getCustomerProductMappingDevice().getTickets();
            if (tickets != null) {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "The device has associated tickets,please delete the tickets before removing the device ");
            } else {
                device.setUser(null);
                deviceRepository.deleteById(deviceId);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.DEVICE_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getAllDevicesBasicDetails(String searchByName, Integer pageNo, Integer pageSize, Integer areaId, String token) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> deviceList = new ArrayList<>();
        Area area = null;
        if (areaId != null) {
            Optional<Area> areaOptional = areaRepository.findById(areaId);
            if (areaOptional.isPresent()) {
                area = areaOptional.get();
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.AREA_NOT_FOUND);
                return map;
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Area finalArea = area;
        User user = utility.getLoggedInUser(token);
        Customer usersCustomer = user.getCustomer();
        Page<Device> devicePage = deviceRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalArea != null) {
                Join<Device, AreaDeviceMapping> areaDeviceMappingJoin = root.join("areaDeviceMapping", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(areaDeviceMappingJoin.get("area"), finalArea));
            }
            if (usersCustomer != null) {
                Join<Device, CustomerProductMappingDevice> deviceCustomerProductMappingDeviceJoin = root.join("customerProductMappingDevice", JoinType.INNER);
                Join<CustomerProductMappingDevice, CustomerProductMapping> customerProductMappingDeviceCustomerProductMappingJoin = deviceCustomerProductMappingDeviceJoin.join("customerProductMapping", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(customerProductMappingDeviceCustomerProductMappingJoin.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
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

    @Override
    public HashMap<String, Object> getTotalDevices(Integer pageNo, Integer pageSize, String searchByName, Integer productId, String token) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> deviceList = new ArrayList<>();
        Product product = null;
        if (productId != null) {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                product = productOptional.get();
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.DEVICE_NOT_FOUND);
                return map;
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Product finalProduct = product;
        User user = utility.getLoggedInUser(token);
        Customer usersCustomer = user.getCustomer();
        Page<Device> devicePage = deviceRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalProduct != null) {
                Join<Device, CustomerProductMappingDevice> deviceCustomerProductMappingDeviceJoin = root.join("customerProductMappingDevice", JoinType.LEFT);
                Join<CustomerProductMappingDevice, CustomerProductMapping> customerProductMappingDeviceCustomerProductMappingJoin = deviceCustomerProductMappingDeviceJoin.join("customerProductMapping", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(customerProductMappingDeviceCustomerProductMappingJoin.get("product"), finalProduct));
            }
            if (usersCustomer != null) {
                Join<Device, CustomerProductMappingDevice> deviceCustomerProductMappingDeviceJoin = root.join("customerProductMappingDevice", JoinType.INNER);
                Join<CustomerProductMappingDevice, CustomerProductMapping> customerProductMappingDeviceCustomerProductMappingJoin = deviceCustomerProductMappingDeviceJoin.join("customerProductMapping", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(customerProductMappingDeviceCustomerProductMappingJoin.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
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