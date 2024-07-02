
package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.IncidentTypeDto;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.service.ProductService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


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
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private IncidentTypeRepository incidentTypeRepository;

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
        product.setName(productDto.getProductName().trim());
        product.setDescription(productDto.getProductDescription());
        product.setProductType(productDto.getProductType());
        User loggedInUser = utility.getLoggedInUser(token);
        product.setUser(loggedInUser);
        Set<IncidentType> incidentTypes1 = new HashSet<>();
        List<IncidentTypeDto> incidentTypeDtos=productDto.getIncidentTypeDtos();
        List<String> type=incidentTypeDtos.stream().map(mapping->mapping.getType()).collect(Collectors.toList());
        long uniqueCount = type.stream().distinct().count();
        if(uniqueCount==type.size()){
            for (IncidentTypeDto incidentTypeDto : incidentTypeDtos) {
                IncidentType incidentType1 = new IncidentType();
                incidentType1.setType(incidentTypeDto.getType());
                incidentType1.setDescription(incidentTypeDto.getDescription());
                incidentType1.setUser(loggedInUser);
                incidentType1.setProduct(product);
                incidentTypes1.add(incidentType1);
            }
        }
        else{
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, "Type should not be same");
            return map;
        }

        product.setIncidentTypes(incidentTypes1);
        Product savedProduct = productRepository.save(product);
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.MESSAGE, Constant.ADD_SUCCESS);
        map.put(Constant.DATA, new HashMap<String, Object>() {{
            put("id", savedProduct.getId());
        }});

        return map;
    }


    @Override
    public HashMap<String, Object> updateProduct(ProductDto productDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Product> productOptional = productRepository.findById(productDto.getId());
        if (productOptional.isPresent()) {
            Product updatedProduct = productOptional.get();
            updatedProduct.setName(productDto.getProductName().trim());
            updatedProduct.setDescription(productDto.getProductDescription());
            updatedProduct.setProductType(productDto.getProductType());
            User loggedInUser = utility.getLoggedInUser(token);
            updatedProduct.setUser(loggedInUser);
            List<IncidentType> existingIncidentTypes = incidentTypeRepository.findByProductId(productDto.getId());
            Map<Integer, IncidentType> existingIncidentTypesMap = existingIncidentTypes.stream()
                    .collect(Collectors.toMap(IncidentType::getId, incidentType -> incidentType));
            Set<IncidentType> updatedIncidentTypes = new HashSet<>();
            List<IncidentTypeDto> incidentTypeDtos=productDto.getIncidentTypeDtos();
            List<String> type=incidentTypeDtos.stream().map(mapping -> mapping.getType()).collect(Collectors.toList());
            long uniqueCount = type.stream().distinct().count();
            if(uniqueCount==type.size()){
                for (IncidentTypeDto incidentTypeDto : incidentTypeDtos) {
                    if (incidentTypeDto.getId() != null && existingIncidentTypesMap.containsKey(incidentTypeDto.getId())) {
                        IncidentType existingIncidentType = existingIncidentTypesMap.get(incidentTypeDto.getId());
                        existingIncidentType.setType(incidentTypeDto.getType());
                        existingIncidentType.setDescription(incidentTypeDto.getDescription());
                        updatedIncidentTypes.add(existingIncidentType);
                        existingIncidentTypesMap.remove(incidentTypeDto.getId());
                    } else if (incidentTypeDto.getId() == null) {
                        IncidentType newIncidentType = new IncidentType();
                        newIncidentType.setType(incidentTypeDto.getType());
                        newIncidentType.setDescription(incidentTypeDto.getDescription());
                        newIncidentType.setUser(loggedInUser);
                        newIncidentType.setProduct(updatedProduct);
                        updatedIncidentTypes.add(newIncidentType);
                    }
                }
                for (IncidentType incidentTypeToRemove : existingIncidentTypesMap.values()) {
                    incidentTypeRepository.delete(incidentTypeToRemove);
                }
            }
            else{
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "Type should not be same");
                return map;
            }
            updatedProduct.setIncidentTypes(updatedIncidentTypes);
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
    public HashMap<String, Object> getAllProducts(Integer pageNo, Integer pageSize, String token, String searchByName) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> productist = new ArrayList<>();
        Customer usersCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Product> productPage = productRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (usersCustomer != null) {
                Join<Product, CustomerProductMapping> customerProductMappingJoin = root.join("customerProductMappings", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(customerProductMappingJoin.get("customer"), usersCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);

        productPage.stream().forEach(product -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", product.getId());
            data.put("productName", product.getName());
            data.put("productDescription", product.getDescription());
            data.put("productType", product.getProductType());
            List<Map<String, Object>> incidentTypes = product.getIncidentTypes().stream()
                    .map(mapping -> {
                        Map<String, Object> deviceData = new HashMap<>();
                        deviceData.put("id", mapping.getId());
                        deviceData.put("type", mapping.getType());
                        deviceData.put("description", mapping.getDescription());
                        return deviceData;
                    })
                    .collect(Collectors.toList());
            data.put("incidentTypes", incidentTypes);

            productist.add(data);
        });


        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, productist);
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
    public HashMap<String, Object> getAllProductsBasicDetails(String token, Integer customerId, String searchByName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> productList = new ArrayList<>();
        Customer customer;
        if (customerId != null) {
            Customer userCustomer = user.getCustomer();
            if (userCustomer != null) {
                if (userCustomer.getId().intValue() == customerId) {
                    customer = customerRepository.findById(customerId).get();
                } else {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, Constant.UNAUTHORIZED);
                    return map;
                }
            } else {
                customer = customerRepository.findById(customerId).get();
            }
        } else {
            customer = user.getCustomer();
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Product> productPage = productRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (customer != null) {
                Join<Product, CustomerProductMapping> customerProductMappingJoin = root.join("customerProductMappings", JoinType.LEFT);
                predicates.add(criteriaBuilder.equal(customerProductMappingJoin.get("customer"), customer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                String searchTerm = "%" + searchByName.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        productPage.stream().forEach(product -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", product.getId());
            data.put("productName", product.getName());
            productList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, productList);
        return map;

    }

    @Override
    public HashMap<String, Object> getAllIncidentTypeByProductId(String token, Integer productId, String searchByType, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        List<HashMap<String, Object>> incidentTypeList = new ArrayList<>();
        Product product = null;
        if (productId != null) {
            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (optionalProduct.isPresent()) {
                product = optionalProduct.get();
            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND);
                return map;
            }
        }
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"type"}).ascending());
        Product finalProduct = product;
        Page<IncidentType> incidentTypePage = incidentTypeRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (finalProduct != null) {
                predicates.add(criteriaBuilder.equal(root.get("product"), finalProduct));
            }
            if (searchByType != null && !searchByType.isEmpty()) {
                String searchTerm = "%" + searchByType.toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("type")), searchTerm));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        incidentTypePage.stream().forEach(incidentType -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", incidentType.getId());
            data.put("type", incidentType.getType());
            incidentTypeList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, incidentTypeList);
        return map;
    }
}

