package com.cms.incidentmanagement.controller;

import com.cms.incidentmanagement.configuration.ExceptionConfig;
import com.cms.incidentmanagement.service.LoginService;
import com.cms.incidentmanagement.utility.Constant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@SecurityRequirement(name = Constant.BEARER_AUTH)
@RestController
@RequestMapping("/")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private LoginService loginService;
    @Autowired
    private ExceptionConfig exceptionConfig;

    @RequestMapping(method = RequestMethod.POST, value = "login")
    public HashMap<String, Object> generateToken(
            @RequestParam("username") String username,
            @RequestParam("password") String password) throws Exception {
        HashMap<String, Object> response;
        try {
            response = loginService.authenticate(username, password);
        } catch (Exception e) {
            response = new HashMap<>();
            response.put(Constant.STATUS, Constant.ERROR);
            response.put(Constant.MESSAGE, e.getMessage());
            logger.error("Error while authenticating user.Message : {}", e.getMessage());
            response = exceptionConfig.getTryCatchErrorMap(e);
        }
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/userInfo")
    @ResponseBody
    @Operation(summary = "Token information API", security = @SecurityRequirement(name = Constant.BEARER_AUTH))
    public HashMap<String, Object> getTokenInfo(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        return loginService.userInfo(token);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/logout")
    public void logoutPage(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getUserInfo")
    public HashMap<String,Object> getUserInfo(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String token) {
        HashMap<String,Object> map = new HashMap<>();
        try {
            map = loginService.getUserInfo(token);
        } catch (Exception e) {
            logger.error("error: " + e.getMessage());
        }
        return map;
    }
}
