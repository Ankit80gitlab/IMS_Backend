package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.dto.ZoneDto;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/13/2024.
 */
public interface ZoneService {
    HashMap<String, Object> addZone(ZoneDto zoneDto, String token);

    HashMap<String, Object> getAllZones(Integer pageNo, Integer pageSize);

    HashMap<String, Object> updateZone(ZoneDto zoneDto, String token);

    HashMap<String, Object> removeZone(Integer zoneId);

    HashMap<String, Object> getZoneInformations(Integer pageNo, Integer pageSize);

    HashMap<String,Object> getZoneAreaInformations(Integer pageNo, Integer pageSize);

    HashMap<String,Object> getAllZonesBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize);
}
