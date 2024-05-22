package com.cms.incidentmanagement.service.implementation;

import com.cms.core.entity.*;
import com.cms.core.repository.*;
import com.cms.incidentmanagement.dto.CustomerDto;
import com.cms.incidentmanagement.dto.ProductDto;
import com.cms.incidentmanagement.dto.RoleDto;
import com.cms.incidentmanagement.service.CustomerService;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Shashidhar on 4/15/2024.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private Utilities utility;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private TicketRepository ticketRepository;


    @Autowired
    private CustomerProductMappingRepository customerProductMappingRepository;

    @Override
    public HashMap<String, Object> addCustomer(CustomerDto customerDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        long existingZoneCount = customerRepository.countByNameIgnoreCase(customerDto.getName());
        if (existingZoneCount > 0) {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_ALREADY_EXISTS);
            return map;
        }

            Customer customer = new Customer();
            customer.setName(customerDto.getName());
            customer.setState(customerDto.getState());
            customer.setCity(customerDto.getCity());
            User loggedInUser = utility.getLoggedInUser(token);
            customer.setUser(loggedInUser);


            List<Integer> productIds = customerDto.getProductIds();
            List<Product> allProducts = productRepository.findAll();

            for (Integer productId : productIds) {
                boolean productFound = allProducts.stream()
                        .anyMatch(product -> product.getId().equals(productId));

                if (!productFound) {
                    map.put(Constant.STATUS, Constant.ERROR);
                    map.put(Constant.MESSAGE, Constant.PRODUCT_NOT_FOUND + "for productId=" + productId);
                    return map;
                }
            }

            List<Product> products = productRepository.findAllById(customerDto.getProductIds());
            Set<CustomerProductMapping> customerProductMappings = new HashSet<>();
            for (Product product : products) {
                CustomerProductMapping customerProductMapping = new CustomerProductMapping();
                customerProductMapping.setCustomer(customer);
                customerProductMapping.setProduct(product);
                customerProductMappings.add(customerProductMapping);
            }
            customer.setCustomerProductMappings(customerProductMappings);
            Customer savedCustomer = customerRepository.save(customer);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.REGISTERED_SUCCESS);
            map.put(Constant.DATA, savedCustomer.getId());

        return map;
    }

    @Override
    public HashMap<String, Object> getAllCustomers(Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        Page<Customer> customers;
        if (pageNo == null) {
            customers = customerRepository.findAll(Pageable.unpaged());
        } else {
            customers = customerRepository.findAll(PageRequest.of(pageNo, pageSize, Sort.by("id").descending()));
        }
        List<Map<String, Object>> customerDataList = new ArrayList<>();
        for (Customer customer : customers) {
            Map<String, Object> customerData = new HashMap<>();
            customerData.put("id", customer.getId());
            customerData.put("name", customer.getName());
            customerData.put("city", customer.getCity());
            customerData.put("state", customer.getState());
            List<Map<String, Object>> productDataList = customer.getCustomerProductMappings().stream()
                    .map(customerProductMapping -> {
                        HashMap<String, Object> productData = new HashMap<>();
                        Product product = customerProductMapping.getProduct();
                        productData.put("id", product.getId());
                        productData.put("name", product.getName());
                        return productData;
                    })
                    .collect(Collectors.toList());

            customerData.put("products", productDataList);
            customerDataList.add(customerData);
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, customerDataList);
        return map;

    }

    @Override
    public HashMap<String, Object> updateCustomer(CustomerDto customerDto, String token) {
        HashMap<String, Object> map = new HashMap<>();
        Optional<Customer> customerOptional = customerRepository.findById(customerDto.getId());
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            customer.setName(customerDto.getName());
            customer.setState(customerDto.getState());
            customer.setCity(customerDto.getCity());

            List<Ticket> tickets = ticketRepository.findByCustomerProductMappingId(customer.getCustomerProductMappings().stream().findFirst().get().getId());
            if (tickets.isEmpty()) {
                Set<CustomerProductMapping> existingMappings = customer.getCustomerProductMappings();
                Set<Integer> existingProductIds = new HashSet<>();
                existingMappings.forEach(mapping -> existingProductIds.add(mapping.getProduct().getId()));
                Set<Integer> newProductIds = new HashSet<>(customerDto.getProductIds());
                Set<Integer> productIdsToRemove = new HashSet<>(existingProductIds);
                productIdsToRemove.removeAll(newProductIds);
                existingMappings.removeIf(mapping -> productIdsToRemove.contains(mapping.getProduct().getId()));
                Set<CustomerProductMapping> customerProductMappings = new HashSet<>();
                for (Integer productId : newProductIds) {
                    if (!existingProductIds.contains(productId)) {
                        Optional<Product> productOptional = productRepository.findById(productId);
                        if (productOptional.isPresent()) {
                            Product product = productOptional.get();
                            CustomerProductMapping newMapping = new CustomerProductMapping();
                            newMapping.setCustomer(customer);
                            newMapping.setProduct(product);
                            customerProductMappingRepository.save(newMapping);
                        } else {
                            map.put(Constant.STATUS, Constant.ERROR);
                            map.put(Constant.MESSAGE, "Product not found for ID: " + productId);
                            return map;
                        }
                    }
                }

            } else {
                map.put(Constant.STATUS, Constant.ERROR);
                map.put(Constant.MESSAGE, "The Customer has pending tickets plz delete them and then try updating");
                return map;
            }
            customerRepository.save(customer);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.UPDATE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> removeCustomer(Integer customerId) {
        HashMap<String, Object> map = new HashMap<>();
        if (customerRepository.findById(customerId).isPresent()) {
            customerRepository.deleteById(customerId);
            map.put(Constant.STATUS, Constant.SUCCESS);
            map.put(Constant.MESSAGE, Constant.DELETE_SUCCESS);
        } else {
            map.put(Constant.STATUS, Constant.ERROR);
            map.put(Constant.MESSAGE, Constant.CUSTOMER_NOT_FOUND);
        }
        return map;
    }

    @Override
    public HashMap<String, Object> getAllCustomersBasicDetails(String token, String searchByName, Integer pageNo, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        User user = utility.getLoggedInUser(token);
        List<HashMap<String, Object>> customerList = new ArrayList<>();
        Customer userCustomer = user.getCustomer();
        PageRequest pageRequest = PageRequest.of(pageNo, pageSize, Sort.by(new String[]{"name"}).ascending());
        Page<Customer> customerPage = customerRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userCustomer != null) {
                predicates.add(criteriaBuilder.equal(root, userCustomer));
            }
            if (searchByName != null && !searchByName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + searchByName + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageRequest);
        customerPage.stream().forEach(customer -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("id", customer.getId());
            data.put("name", customer.getName());
            customerList.add(data);
        });
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, customerList);
        return map;
    }

    @Override
    public HashMap<String, Object> getAllProductsOfCustomer(String token, Integer pageNo, Integer pageSize, Integer customerId) {
        HashMap<String, Object> map = new HashMap<>();
        List<CustomerProductMapping> customerProductMappings = customerProductMappingRepository.findBycustomerId(customerId);
        List<Integer> productIds = customerProductMappings.stream()
                .map(mapping -> mapping.getProduct().getId())
                .collect(Collectors.toList());
        List<Map<String, Object>> productDataList = new ArrayList<>();
        for (Integer productId : productIds) {
            Optional<Product> productOptional = productRepository.findById(productId);
            if (productOptional.isPresent()) {
                Product product = productOptional.get();
                HashMap<String, Object> productData = new HashMap<>();
                productData.put("id", product.getId());
                productData.put("name", product.getName());
                productDataList.add(productData);
            }
        }
        map.put(Constant.STATUS, Constant.SUCCESS);
        map.put(Constant.DATA, productDataList);
        return map;
    }
}