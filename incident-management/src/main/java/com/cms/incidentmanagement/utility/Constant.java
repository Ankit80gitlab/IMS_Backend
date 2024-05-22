package com.cms.incidentmanagement.utility;

public interface Constant {

    String HOME_URI = "/";

    String TOKEN = "token";

    String ACCESS_TOKEN = "access_token";

    String TOKEN_TYPE = "token_type";

    String BEARER = "bearer";

    String LOGIN_URI = "/login";

    String BEARER_AUTH = "bearerAuth";

    String AUTHORITIES_KEY = "authority_key";

    String USER_ID = "user_id";

    String PASSWORD_KEY = "password_key";

    String STATUS = "status";

    String SUCCESS = "success";

    String ERROR = "error";

    String MESSAGE = "message";

    String TEXT = "text";

    String DELETE_SUCCESS = "Successfully deleted";

    String UPDATE_SUCCESS = "Successfully updated";

    String CREATE_SUCCESS = "Successfully created";

    String ROLE_NAME_UPDATE_SUCCESS = "Role name updated successfully";

    String DUPLICATE_ROLE_NAME = "Duplicate role name";

    String TRY_AGAIN_LATER = "Please try again later";

    String USER_ROLE = "userRole";

    String USER_NAME = "userName";

    String CHANGE_SUCCESS = "Successfully changed ";

    String FILE_NOT_FOUND = "File Not Found";

    String ROLE_ID_NOT_FOUND = "Role id not found";

    String FETCHED_ALL_TICKETS = "Fetched all tickets";

    String ROLE_ASSIGNED_TO_USER = "Role assigned to user";

    String USER_NOT_FOUND = "User Not Found";

    String USERNAME_ALREADY_EXIST = "Username already exist";

    String INCORRECT_USERNAME = "Incorrect username";

    String INCORRECT_PASSWORD = "Incorrect password";

    String REGISTERED_SUCCESS = "Registered successfully";

    String PASSWORD_CHANGED_SUCCESS = "Password changed successfully";

    String SAME_OLD_NEW_PASSWORD = "New password cannot be same as old password";

    String REASON = "reason";

    String DATA = "data";

    String[] SKIP_URLS = {"/instances", "/actuator/**", "/img/**", "/css/**", "/js/**", "/plugins/**", "/**/*.js",
            "/**/*.css", "/**/*.ico", "/**/*.jpg", "/**/*.jpeg", "/**/*.png", "/apiTest/**", "/v3/api-docs/**",
            "/swagger-ui/**", "/login", "/logout", "/testing/**"};

    String USER_MANAGEMENT = "User Management";
    String ROLE_MANAGEMENT = "Role Management";
    String FEATURE_MANAGEMENT = "Feature Management";
    String TICKET_MANAGEMENT = "Ticket Management";
    String USER_MANAGEMENT_URL = "/userManagement/**";
    String ROLE_MANAGEMENT_URL = "/roleManagement/**";
    String TICKET_MANAGEMENT_URL = "/ticketManagement/**";
    String FEATURE_MANAGEMENT_URL = "/featureManagement/**";

    String[] USER_MANAGEMENT_URL1 = {
            "/userManagement/updateUserBasic/",
            "/userManagement/updateUserRole",
            "/userManagement/removeUser/",
    };

    String[] ROLE_MANAGEMENT_URL1 = {
            "/roleManagement/getAllRoles",
            "/roleManagement/createRole",
            "/roleManagement/deleteRole/",
            "/featureManagement/getAllFeatures",
            "/featureManagement/getRoleFeatures/",
    };

    String PRODUCT_ALREADY_EXISTS = "Product already exist";
    String CUSTOMER_ALREADY_EXISTS = "Customer already exist";

    String PRODUCT_NOT_FOUND = "Product not found";

    String CUSTOMER_NOT_FOUND = "Customer not found";

    String ROLE_NOT_FOUND = "Role not found";
    String FEATURE_NOT_FOUND = "Feature not found";

    String DEVICE_ALREADY_EXISTS = "Device already exist";
    String DEVICE_NOT_FOUND = "Device not found";
    String ZONE_ALREADY_EXISTS = "Zone already exist";
    String ZONE_NOT_FOUND = "Zone not found";
    String CUSTOMER_PRODUCT_MAPPING_NOT_FOUND = "CustomerProductMapping not found";
    String AREA_NOT_FOUND = "Area not found";
    String AREA_ALREADY_EXISTS = "Area already exist";
    String EMAIL_ALREADY_EXIST = "Email already exist";
}
