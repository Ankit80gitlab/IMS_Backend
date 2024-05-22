package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.UserDto;

import java.util.HashMap;

public interface UserManagementService {

	HashMap<String, Object> getAllUsers(Integer pageNo, Integer pageSize);

	HashMap<String, Object> updateUser( UserDto userDto);

	HashMap<String, Object> changePassword(String token,String currentPassword,String newPassword);

	HashMap<String, Object> removeUser(Integer userId);

	HashMap<String, Object> registerUser(UserDto userDto, String token);

	HashMap<String,Object> getAllUserBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize);
}
