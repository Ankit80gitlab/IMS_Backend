package com.cms.incidentmanagement.configuration;

import com.cms.incidentmanagement.filter.JwtFilter;
import com.cms.incidentmanagement.handler.CustomAccessDeniedHandler;
import com.cms.incidentmanagement.utility.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Value("#{'${cors.allowed-origins}'.split(',')}")
    private List<String> allowedOrigins;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .headers().frameOptions().sameOrigin().and()
                .cors(cors -> {
                    cors.configurationSource(request -> {
                        CorsConfiguration configuration = new CorsConfiguration();
                        configuration.setAllowedOrigins(allowedOrigins);
                        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        configuration.setAllowedHeaders(Arrays.asList("content-type", "x-auth-token", "Authorization"));
                        configuration.setAllowCredentials(true);
                        configuration.setMaxAge(3600L); // 1 hour
                        return configuration;
                    });
                })
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .logout().disable()
                .authorizeRequests()
                .antMatchers("/login").permitAll()
              .antMatchers(Constant.SKIP_URLS).permitAll()
                .antMatchers("/userManagement/getAllUser",
                        "/userManagement/register",
                        "/userManagement/updateUserBasic",
                        "/userManagement/updateUserRole",
                        "/userManagement/changePassword",
                        "/userManagement/removeUser",
                        "/roleManagement/getAllRoles").hasAuthority(Constant.USER_MANAGEMENT)

                .antMatchers("/roleManagement/getAllRoles",
                        "/roleManagement/createRole",
                        "/roleManagement/deleteRole",
                        "/featureManagement/getAllFeatures",
                        "/featureManagement/getRoleFeatures",
                        "/featureManagement/updateRoleFeature").hasAuthority("Role Management")

                .antMatchers("/featureManagement/getAllFeatures",
                        "/featureManagement/getRoleFeatures",
                        "/featureManagement/updateRoleFeature").hasAuthority("Feature Management")

                .antMatchers("/ticketManagement/getAllTickets",
                        "/ticketManagement/create",
                        "/ticketManagement/delete").hasAuthority(Constant.TICKET_MANAGEMENT)

                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
                .and()
                .addFilterBefore(
                        jwtFilter, UsernamePasswordAuthenticationFilter.class
                ).
                sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}