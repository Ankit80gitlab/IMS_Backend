package com.cms.incidentmanagement.utility;

import com.cms.core.entity.User;
import com.cms.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Utilities {

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	public User getLoggedInUser(String token) {
		if (token.startsWith("Bearer")) {
			token = token.substring(7);
		}
		return userRepository.findByUsername(jwtUtil.getUsernameFromToken(token));
	}
}
