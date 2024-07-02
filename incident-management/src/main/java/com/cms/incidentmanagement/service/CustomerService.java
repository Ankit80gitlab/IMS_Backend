package com.cms.incidentmanagement.service;

import com.cms.incidentmanagement.dto.CustomerDto;
import com.cms.incidentmanagement.dto.ProductDto;

import java.util.HashMap;

/**
 * Created by Shashidhar on 4/15/2024.
 */
public interface CustomerService {
    HashMap<String, Object> addCustomer(CustomerDto customerDto,String token);

    HashMap<String, Object> getAllCustomers(Integer pageNo, Integer pageSize,String searchByName,String token);

    HashMap<String, Object> updateCustomer( CustomerDto customerDto,String token);

    HashMap<String, Object> removeCustomer(Integer customerId);

	HashMap<String, Object> getAllCustomersBasicDetails(String token,String searchByName, Integer pageNo, Integer pageSize);

    HashMap<String, Object> getAllProductsOfCustomer(String token, Integer pageNo, Integer pageSize,Integer customerId);

    HashMap<String,Object> getCustomersProductDetails(String token, Integer pageNo, Integer pageSize, Integer customerId, Integer productId);

    HashMap<String,Object> updateCustomerProductDetails(ProductDto productDto, String token, Integer customerId);

    HashMap<String,Object> removeCustomersProduct(String token, Integer customerId, Integer productId);
}

