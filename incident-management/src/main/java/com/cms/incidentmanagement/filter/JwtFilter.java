package com.cms.incidentmanagement.filter;

import com.cms.incidentmanagement.utility.Constant;
import com.cms.incidentmanagement.utility.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String username = null;
		String jwtToken = null;
		String servletPath = request.getServletPath();
		final String requestTokenHeader = request.getHeader("Authorization");
		if (requestTokenHeader != null) {
			if (requestTokenHeader.startsWith("Bearer ")) {
				jwtToken = requestTokenHeader.substring(7);
			} else {
				logger.warn("JWT Token does not begin with Bearer String");
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT Token does not begin with Bearer String.");
			}
		} else if (request.getParameter("token") != null) {
			jwtToken = request.getParameter("token");
		}
		if (jwtToken != null) {
			try {
				username = jwtUtil.getUsernameFromToken(jwtToken);
			} catch (IllegalArgumentException e) {
				logger.error("Unable to get JWT Token");
			} catch (ExpiredJwtException e) {
				logger.debug("JWT Token has expired");
			}
		}
		String loginPath = request.getContextPath();
		if (username != null) {
			if (SecurityContextHolder.getContext().getAuthentication() == null) {
				if (jwtUtil.validateToken(jwtToken, username)) {
					SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthenticationToken(jwtToken, username));
					chain.doFilter(request, response);
				} else {
					if (request.getMethod().equalsIgnoreCase("POST")) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} else {
						response.sendRedirect(loginPath);
					}
					SecurityContextHolder.clearContext();
				}
			} else {
				if (jwtUtil.validateToken(jwtToken, username)) {
					chain.doFilter(request, response);
				} else {
					if (request.getMethod().equalsIgnoreCase("POST")) {
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} else {
						response.sendRedirect(loginPath);
					}
					SecurityContextHolder.clearContext();
				}
			}
		} else {
			if (new AntPathMatcher().match(Constant.HOME_URI, servletPath)) {
				chain.doFilter(request, response);
			} else {
				if (request.getMethod().equalsIgnoreCase("POST")) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					response.sendRedirect(loginPath);
				}
				SecurityContextHolder.clearContext();
			}
		}
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		for (String matcher : Constant.SKIP_URLS) {
			if (new AntPathMatcher().match(matcher, request.getServletPath())) {
				return true;
			}
		}
		return false;
	}

}
