package com.cms.incidentmanagement.service;

import com.cms.core.entity.UserRoleMapping;
import com.cms.incidentmanagement.configuration.CustomAuthenticationProvider;
import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.JwtUtil;
import com.cms.incidentmanagement.utility.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Utilities utilities;

    public HashMap<String, Object> authenticate(String username, String password) throws Exception {
        HashMap<String, Object> response = new HashMap<>();
        try {
            Authentication authenticate = customAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            if (authenticate != null) {
                User user = (User) authenticate.getPrincipal();
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                String token = jwtUtil.generateToken(user);
                response.put(Constant.ACCESS_TOKEN, token);
                response.put(Constant.TOKEN_TYPE, Constant.BEARER);
            } else {
                response.put(Constant.STATUS, Constant.ERROR);
                response.put(Constant.MESSAGE, "UnAuthorized");
            }
        } catch (DisabledException e) {
            logger.error("Error while authenticating user", e);
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            logger.error("Error while authenticating user", e);
            throw new Exception("INVALID_CREDENTIALS", e);
        }
        return response;
    }

    public HashMap<String, Object> userInfo(String token) {
        HashMap<String, Object> userInfo = new HashMap<>();
        com.cms.core.entity.User user = utilities.getLoggedInUser(token);
        Set<UserRoleMapping> roleMappings = user.getUserRoleMappings();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("roles", roleMappings.stream().map(userRoleMapping -> userRoleMapping.getRole().getName()).collect(Collectors.toList()));
        userInfo.put("permissions",
                roleMappings.stream().flatMap(userRoleMapping -> userRoleMapping.getRole().getRoleFeatureMappings().stream()
                        .map(roleFeatureMapping -> roleFeatureMapping.getFeature().getName())).collect(Collectors.toList()));

        return userInfo;
    }
}
