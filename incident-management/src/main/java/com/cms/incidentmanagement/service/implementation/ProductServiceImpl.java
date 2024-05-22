
package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.Device;
import com.cms.core.entity.Product;
import com.cms.core.entity.User;
import com.cms.core.repository.CustomerProductMappingRepository;
import com.cms.core.repository.ProductRepository;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.service.ProductService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.*;

/**
 * Created by Shashidhar on 4/15/2024.
 */
@Service
public class ProductServiceImpl implements ProductService {


    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Utilities utility;

    @Autowired
    private CustomerProductMappingRepository customerProductMappingRepository;

    @Transactional
    @Override
    public HashMap<String, Object> addProduct(ProductDto productDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = productRepository.countByNameIgnoreCase(productDto.getProductName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.PRODUCT_ALREADY_EXISTS);
            return map;
        }
            Product product = new Product();
            product.setName(productDto.getProductName());
            product.setDescription(productDto.getProductDescription());
            product.setProductType(productDto.getProductType());
            User loggedInUser = utility.getLoggedInUser(token);
            product.setUser(loggedInUser);
            Product savedProduct = productRepository.save(product);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.REGISTERED_SUCCESS);
            map.put(Constant.DATA, savedProduct.getId());
        return map;
    }

    @Transactional
    @Override
    public HashMap<String, Object> updateProduct(ProductDto productDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Product> productOptional = productRepository.findById(productDto.getId());
        if (productOptional.isPresent()) {
            Product updatedProduct = productOptional.get();
            updatedProduct.setName(productDto.getProductName());
            updatedProduct.setDescription(productDto.getProductDescription());
            updatedProduct.setProductType(productDto.getProductType());
            User loggedInUser = utility.getLoggedInUser(token);
            updatedProduct.setUser(loggedInUser);
            productRepository.save(updatedProduct);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND);
        }

        return map;
    }


    @Override
    public HashMap<String, Object> getAllProducts(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Page<Product> products;
        if (pageNo == null) {
            products = productRepository.findAll(Pageable.unpaged());
        } else {
            products = productRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("id").descending()));
        }
        List<ProductDto> productDtoList = new ArrayList<>();
        for (Product product : products) {
            ProductDto dto = new ProductDto();
            dto.setId(product.getId());
            dto.setProductName(product.getName());
            dto.setProductDescription(product.getDescription());
            dto.setProductType(product.getProductType());
            productDtoList.add(dto);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, productDtoList);
        return map;
    }

    @Transactional
    @Override
    public HashMap<String, Object> removeProduct(Integer productId) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            boolean isAssociated = customerProductMappingRepository.existsByProductId(productId);
            if (isAssociated) {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "Cannot delete product because it is linked to one or more customers");
            } else {
                product.setUser(null);
                productRepository.deleteById(productId);
                map.put(Constant.STATUS, Constant.SUCCESS);
                map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
            }
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> searchProducts(String productName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Page<Product> productPage;
        if (pageNo == null || pageSize == null) {
            productPage = productRepository.findByNameContainingIgnoreCase(productName, Pageable.unpaged());
        } else {
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
            productPage = productRepository.findByNameContainingIgnoreCase(productName, pageable);
        }
        List<Product> products = productPage.getContent();
        List<ProductDto> productDtoList = new ArrayList<>();
        if (products != null && !products.isEmpty()) {
            for (Product product : products) {
                ProductDto dto = new ProductDto();
                dto.setId(product.getId());
                dto.setProductName(product.getName());
                dto.setProductDescription(product.getDescription());
                dto.setProductType(product.getProductType());
                productDtoList.add(dto);
            }
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.DATA, productDtoList);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND);
            map.put(Constant.DATA, productDtoList);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getProducts() {
        HashMap<String, Object> map = new HashMap<>();
        List<Product> products = productRepository.findAll();
        List<Map<String, Object>> productDataList = new ArrayList<>();
        for (Product product : products) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", product.getId());
            productData.put("productName", product.getName());
            productDataList.add(productData);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, productDataList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllProductsBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize){

            HashMap<String, Object> map = new HashMap<>();
            User user = utility.getLoggedInUser(token);
            List<HashMap<String, Object>> productist = new ArrayList<>();
            Set<Product> userProducts = user.getProducts();
            PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
            Page<Product> productPage = productRepository.findAll((root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (!userProducts.isEmpty()) {
                    predicates.add(root.in(userProducts));
                }
                if(searchByName != null && !searchByName.isEmpty()) {
                    String searchTerm = "%" + searchByName.toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }, pageRequest);
            productPage.stream().forEach(product -> {
                HashMap<String, Object> data = new HashMap<>();
                data.put("id", product.getId());
                data.put("name", product.getName());
                productist.add(data);
            });
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.DATA, productist);
            return map;

    }
}

