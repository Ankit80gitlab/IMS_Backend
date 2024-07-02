package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.AreaDto;
import com.cms.incidentmanagement.dto.ZoneDto;

import java.util.HashMap;

/**
 * Created by Shashidhar on 5/14/2024.
 */
public interface AreaService {

    HashMap<String, Object> addArea(AreaDto areaDto, String token);

    HashMap<String, Object> getAllAreas(String token, Integer pageNo, Integer pageSize, String searchByName, Integer zoneId);

    HashMap<String, Object> updateArea(AreaDto areaDto, String token);

    HashMap<String, Object> removeArea(Integer areaId);

    HashMap<String, Object> getAllAreasBasicDetails(String searchByName, Integer pageNo, Integer pageSize, Integer zoneId,String token);


}
