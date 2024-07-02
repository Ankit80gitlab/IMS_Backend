package com.cms.incidentmanagement.utility;

import com.cms.core.entity.Role;
import com.cms.core.entity.UserRoleMapping;
import com.cms.core.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil implements Serializable {

    public static final long JWT_TOKEN_VALIDITY = 120 * 60;
    private static final long serialVersionUID = -2550185165626007488L;
    private UserRepository userRepository;

    public JwtUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean ignoreTokenExpiration(String token) {
        return false;
    }

    public String generateToken(User user) {
        com.cms.core.entity.User usr = userRepository.findByUsername(user.getUsername());
        int userId = usr.getId();
        String userName = usr.getName();
        String userRoleName = null;
        Set<UserRoleMapping> userRoleMappings = usr.getUserRoleMappings();
        for (UserRoleMapping userRoleMapping : userRoleMappings) {
            Role role = userRoleMapping.getRole();
            if (role != null) {
                userRoleName = role.getName();
                break;
            }
        }

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.USER_ID, userId);
        claims.put(Constant.USER_ROLE, userRoleName);
        claims.put(Constant.USER_NAME, userName);
        claims.put(Constant.AUTHORITIES_KEY, authorities);
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(key)
                .compact();
    }

    public Boolean canTokenBeRefreshed(String token) {
        return (!isTokenExpired(token) || ignoreTokenExpiration(token));
    }

    public Boolean validateToken(String token, String userName) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userName) && !isTokenExpired(token));
    }

    public UsernamePasswordAuthenticationToken getAuthenticationToken(String token, String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        Claims claims = claimsJws.getPayload();
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(Constant.AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
