package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.CustomerDto;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.service.implementation.CustomerServiceImpl;
import com.cms.incidentmanagement.service.implementation.ProductServiceImpl;
import com.cms.incidentmanagement.utility.Constant;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Shashidhar on 4/15/2024.
 */@SecurityRequirement(name = Constant.BEARER_AUTH)
   @RestController
   @RequestMapping("/customerManagement")
   public class CustomerController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    @Autowired
    private  CustomerServiceImpl customerServiceImpl;
    @Autowired
    private  ExceptionConfig exceptionConfig;
    @PostMapping("/addCustomer")
    public HashMap<String, Object> addCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody CustomerDto customerDto) {
        HashMap<String, Object> addCustomerMap;
        try {

            addCustomerMap = customerServiceImpl.addCustomer(customerDto,token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            addCustomerMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return addCustomerMap;
    }


    @GetMapping("/getAllCustomers")
    public HashMap<String, Object> getAllCustomers(
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        HashMap<String, Object> map;
        try {
            map = customerServiceImpl.getAllCustomers(pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @PutMapping("/updateCustomer")
    public HashMap<String, Object> updateCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody CustomerDto customerDto) {
        HashMap<String, Object> map;
        try {
            map = customerServiceImpl.updateCustomer(customerDto, token);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @DeleteMapping("/deleteCustomer")
    public HashMap<String, Object> removeCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "customerId", required = false) Integer customerId) {
        HashMap<String, Object> map;
        try {
            map = customerServiceImpl.removeCustomer(customerId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllCustomersBasicDetails")
    public HashMap<String, Object> getAllCustomersBasicDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
         ) {
        HashMap<String, Object> map;
        try {
            map = customerServiceImpl.getAllCustomersBasicDetails(token,searchByName, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllProductsOfCustomer")
    public HashMap<String, Object> getAllProductsOfCustomer(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "customerId", required = false) Integer customerId) {
        HashMap<String, Object> map;
        try {
            map = customerServiceImpl.getAllProductsOfCustomer(token, pageNo, pageSize,customerId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

}

