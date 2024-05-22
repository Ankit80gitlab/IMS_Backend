package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.DeviceDto;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/13/2024.
 */
public interface DeviceManagementService {

    HashMap<String, Object> addDevice(DeviceDto deviceDto,String token);

    HashMap<String, Object> getAllDevices(Integer pageNo, Integer pageSize);

    HashMap<String, Object> updateDevice( DeviceDto deviceDto,String token);

    HashMap<String, Object> removeDevice(Integer deviceId);

    HashMap<String, Object> getCustomerProductMappings();

    HashMap<String, Object> getAllDevicesBasicDetails(String token,String searchByName, Integer pageNo, Integer pageSize);

}
