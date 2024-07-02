
package com.cms.incidentmanagement.controller;

import com.cms.core.entity.Product;
import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.dto.ProductDto;

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
 */
@SecurityRequirement(name = Constant.BEARER_AUTH)
@RestController
@RequestMapping("/productManagement")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductServiceImpl productServiceImpl;
    @Autowired
    private ExceptionConfig exceptionConfig;

    @PostMapping("/addProduct")
    public HashMap<String, Object> addProduct(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody ProductDto productDto) {
        HashMap<String, Object> addCustomerMap;
        try {
            addCustomerMap = productServiceImpl.addProduct(productDto, token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
            addCustomerMap = exceptionConfig.getTryCatchErrorMap(e);
        }
        return addCustomerMap;
    }

    @PutMapping("/updateProduct")
    public HashMap<String, Object> updateProduct(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestBody ProductDto productDto) {
        HashMap<String, Object> map;
        try {
            map = productServiceImpl.updateProduct(productDto, token);
        } catch (Exception e) {
            logger.error("error : " + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllProducts")
    public HashMap<String, Object> getAllProducts(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "searchByName", required = false) String searchByName) {
        HashMap<String, Object> map;
        try {
            map = productServiceImpl.getAllProducts(pageNo, pageSize, token, searchByName);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }


    @DeleteMapping("/deleteProduct")
    public HashMap<String, Object> removeProduct(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "productId", required = false) Integer productId) {
        HashMap<String, Object> map;
        try {
            map = productServiceImpl.removeProduct(productId);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }


    @GetMapping("/getAllProductsBasicDetails")
    public HashMap<String, Object> getAllProductsBasicDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "customerId", required = false) Integer customerId,
            @RequestParam(name = "searchByName", required = false) String searchByName,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        HashMap<String, Object> map;
        try {
            map = productServiceImpl.getAllProductsBasicDetails(token, customerId, searchByName, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }

    @GetMapping("/getAllIncidentTypeByProductId")
    public HashMap<String, Object> getAllIncidentTypeByProductId(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(name = "searchByType", required = false) String searchByType,
            @RequestParam(name = "pageNo", required = false) Integer pageNo,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(name = "productId") Integer productId
    ) {
        HashMap<String, Object> map;
        try {
            map = productServiceImpl.getAllIncidentTypeByProductId(token, productId, searchByType, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("error :" + e.getMessage());
            map = exceptionConfig.getTryCatchErrorMap(e);
        }
        return map;
    }




}
