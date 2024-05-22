package com.cms.incidentmanagement.configuration;

import com.cms.core.entity.*;
import com.cms.core.repository.RoleRepository;
import com.cms.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LDAPAuthenticationProvider ldapAuthenticationProvider;

	@Autowired
	private RoleRepository roleRepository;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		String name = authentication.getName();
		String password = authentication.getCredentials().toString();
		User user = userRepository.findByUsername(name);
		boolean isAuthenticated = false;
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
	if (user == null || (user.getLdapUser())) {
			HashMap<String, Object> map = ldapAuthenticationProvider.authenticateUser(name, password);	        if ((boolean) map.get("isAuthenticated")) {
				if (user == null) {
					user = new User();
					user.setEmail(String.valueOf(map.get("mail")));
					user.setPassword(passwordEncoder().encode(password));
					user.setCreatedTime(currentTime);
					user.setUsername(name);
					user.setName(String.valueOf(map.get("displayName")));
					user.setLdapUser(true);

				}
            Role role = roleRepository.findOneByName("LDAP_USER");
            if (role == null) {
                role = new Role();
                role.setName("LDAP_USER");
                role.setUser(user);
                role = roleRepository.save(role);
            }

            // Update user's role mapping
            UserRoleMapping userRoleMapping = new UserRoleMapping();
            userRoleMapping.setRole(role);
            userRoleMapping.setUser(user);
            user.getUserRoleMappings().add(userRoleMapping);


            isAuthenticated = true;
			}
		} else if (passwordEncoder().matches(password, user.getPassword())) {
			isAuthenticated = true;
		}
		if (isAuthenticated) {
			user.setLoginTime(currentTime);
			userRepository.save(user);
			Set<SimpleGrantedAuthority> authorities = getAuthorities(user);
			org.springframework.security.core.userdetails.User user1 = new org.springframework.security.core.userdetails.User(name, password, authorities);
			return new UsernamePasswordAuthenticationToken(user1, password, authorities);
		}
		return null;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

    @Transactional
    public Set<SimpleGrantedAuthority> getAuthorities(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (user.getUserRoleMappings() != null) {
            for (UserRoleMapping userRoleMapping : user.getUserRoleMappings()) {
                Role role = userRoleMapping.getRole();
                if (role != null) {
                    Set<RoleFeatureMapping> roleFeatureMappings = role.getRoleFeatureMappings();
                    if (roleFeatureMappings != null) {
                        for (RoleFeatureMapping roleFeatureMapping : roleFeatureMappings) {
                            Feature feature = roleFeatureMapping.getFeature();
                            if (feature != null) {
                                authorities.add(new SimpleGrantedAuthority(feature.getName()));
                            }
                        }
                    }
                }
            }
        }

        return authorities;
    }
}
