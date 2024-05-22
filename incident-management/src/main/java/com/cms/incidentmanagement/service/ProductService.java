package com.cms.incidentmanagement.service;

import com.cms.core.entity.Product;
import com.cms.incidentmanagement.dto.ProductDto;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Shashidhar on 4/15/2024.
 */
public interface ProductService {
    HashMap<String, Object> addProduct(ProductDto productDto, String token);

    HashMap<String, Object> getAllProducts(Integer pageNo, Integer pageSize);

    HashMap<String, Object> updateProduct(ProductDto productDto, String token);

    HashMap<String, Object> removeProduct(Integer productId);

    HashMap<String, Object> searchProducts(String productName,Integer pageNo, Integer pageSize);

    HashMap<String, Object> getProducts();

    HashMap<String, Object> getAllProductsBasicDetails(String token,String searchByName, Integer pageNo, Integer pageSize);
}

