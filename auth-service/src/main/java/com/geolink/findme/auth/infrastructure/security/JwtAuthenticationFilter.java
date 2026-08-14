package com.geolink.findme.auth.infrastructure.security;

import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.TokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Revalide indépendamment le JWT déjà vérifié par l'API Gateway (défense en profondeur, voir
 * docs/conception §7.1). N'échoue jamais silencieusement : un token absent laisse la requête
 * anonyme (les routes publiques restent accessibles), un token présent mais invalide est rejeté.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtIssuerPort jwtIssuer;

    public JwtAuthenticationFilter(JwtIssuerPort jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                TokenClaims claims = jwtIssuer.parseAndValidate(token);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));
                var authentication = new UsernamePasswordAuthenticationToken(claims.userId().toString(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (InvalidOrExpiredTokenException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
