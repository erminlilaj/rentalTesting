package it.linksmt.rental.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import it.linksmt.rental.entity.UserEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        }catch (Exception e) {
            throw new ServiceException(ErrorCode.INVALID_TOKEN,
                    "Invalid token");
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            throw new ServiceException(
                    ErrorCode.INVALID_TOKEN,
                    "Invalid token"
            );
        }
    }

    public String generateToken(UserEntity userEntity) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userEntity.getId());
        claims.put("username", userEntity.getUsername());
        claims.put("name", userEntity.getName());
        claims.put("surname", userEntity.getSurname());
        claims.put("email", userEntity.getEmail());
        claims.put("age", userEntity.getAge());
        claims.put("userType", userEntity.getUserType().name());
        claims.put("roles", userEntity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        return buildToken(claims, userEntity.getUsername(), jwtExpiration);
    }
    public long getExpirationTime() {
        return jwtExpiration;
    }
    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        if (!username.equals(userDetails.getUsername()) || isTokenExpired(token)) {

            throw new ServiceException(
                    ErrorCode.INVALID_TOKEN
            );

        }

        return true;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
