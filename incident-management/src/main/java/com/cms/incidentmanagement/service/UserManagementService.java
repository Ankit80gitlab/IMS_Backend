package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.UserDto;

import java.util.HashMap;

public interface UserManagementService {

	HashMap<String, Object> getAllUsers(Integer pageNo, Integer pageSize,String token,String searchByUsername);

	HashMap<String, Object> updateUser( UserDto userDto,String token);

	HashMap<String, Object> changePassword(String token,String currentPassword,String newPassword);

	HashMap<String, Object> removeUser(Integer userId,String token);

	HashMap<String, Object> registerUser(UserDto userDto, String token);

	HashMap<String,Object> getAllUserBasicDetails(String token, String searchByUsername, Integer pageNo, Integer pageSize);

	HashMap<String, Object> getAllCustomersUser(Integer pageNo, Integer pageSize,String token,String searchByUsername);

	HashMap<String, Object> getAllUsersOfCustomer(Integer pageNo, Integer pageSize,String searchByUsername,Integer customerId);

	HashMap<String,Object> getAllTypesForUser(Integer pageNo, Integer pageSize, String searchByName);
}
