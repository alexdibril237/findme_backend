package com.geolink.findme.gateway.security;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Rejette au plus tôt les requêtes non authentifiées vers les routes protégées (voir la matrice
 * RBAC, docs/conception §6, colonnes 🌐 = publiques). Les routes publiques laissent passer la
 * requête sans exiger de token ; toutes les autres exigent un Authorization: Bearer valide.
 */
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    private record PublicRoute(HttpMethod method, String pattern) {
    }

    private static final List<PublicRoute> PUBLIC_ROUTES = List.of(
            new PublicRoute(HttpMethod.POST, "/api/auth/signup"),
            new PublicRoute(HttpMethod.POST, "/api/auth/signin"),
            new PublicRoute(HttpMethod.POST, "/api/auth/refresh"),
            new PublicRoute(HttpMethod.POST, "/api/auth/forgot-password"),
            new PublicRoute(HttpMethod.POST, "/api/auth/reset-password"),
            new PublicRoute(HttpMethod.POST, "/api/support"),
            new PublicRoute(HttpMethod.GET, "/files/**"),
            new PublicRoute(HttpMethod.GET, "/actuator/**")
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final GatewayJwtValidator jwtValidator;
    private final ObjectMapper objectMapper;

    public GatewayAuthFilter(GatewayJwtValidator jwtValidator, ObjectMapper objectMapper) {
        this.jwtValidator = jwtValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isPublicRoute(request)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring("Bearer ".length()) : null;

        if (token == null || !jwtValidator.isValid(token)) {
            writeUnauthorized(response);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicRoute(HttpServletRequest request) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String path = request.getRequestURI();
        return PUBLIC_ROUTES.stream()
                .anyMatch(route -> route.method() == method && pathMatcher.match(route.pattern(), path));
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Authentification requise ou token invalide/expiré");
        problem.setProperty("errorCode", "UNAUTHENTICATED");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), problem);
    }
}
