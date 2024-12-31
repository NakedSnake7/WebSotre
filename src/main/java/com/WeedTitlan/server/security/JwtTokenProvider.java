package com.WeedTitlan.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Lazy;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

	private final String secretKey = Base64.getEncoder().encodeToString("tuClaveSecreta12345	".getBytes()); // Asegúrate de que esta clave sea adecuada y segura
    private final long validityInMilliseconds = 3600000; // 1 hora

    private final UserDetailsService userDetailsService; // Dependencia inyectada por el constructor

    public JwtTokenProvider(@Lazy UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Método para crear el token
    public String createToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
            .compact();
    }

    // Método para resolver el token desde el header
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Método para validar el token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false; // Si ocurre alguna excepción (como expiración, firma no válida)
        }
    }

    // Método para obtener la autenticación desde el token
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token); // Extrae el username del token
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // Método para extraer el username del token
    private String getUsernameFromToken(String token) {
        Jws<Claims> parsedClaims = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
            .build()
            .parseClaimsJws(token);

        return parsedClaims.getBody().get("sub", String.class);
    }
}
