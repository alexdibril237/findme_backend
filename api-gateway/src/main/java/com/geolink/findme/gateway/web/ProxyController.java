package com.geolink.findme.gateway.web;

import com.geolink.findme.gateway.config.RouteDefinition;
import com.geolink.findme.gateway.config.RouteResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;

/**
 * Passerelle de routage générique : forwarde chaque requête vers le microservice résolu par
 * {@link RouteResolver} en préservant méthode, en-têtes, query params et corps tel quel (JSON,
 * multipart, binaire) — voir docs/conception §2.5.
 */
@RestController
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection", "keep-alive", "transfer-encoding", "content-length", "host");

    private final RouteResolver routeResolver;
    private final RestClient proxyRestClient;

    public ProxyController(RouteResolver routeResolver, RestClient proxyRestClient) {
        this.routeResolver = routeResolver;
        this.proxyRestClient = proxyRestClient;
    }

    @RequestMapping("/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        var routeOptional = routeResolver.resolve(path);
        if (routeOptional.isEmpty()) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                    "Aucune route configurée pour : " + path);
            problem.setProperty("errorCode", "GATEWAY_NO_ROUTE");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem.toString().getBytes());
        }
        RouteDefinition route = routeOptional.get();

        String targetUrl = UriComponentsBuilder.fromUriString(route.getBaseUrl())
                .path(path)
                .query(request.getQueryString())
                .build(true)
                .toUriString();

        byte[] body = request.getInputStream().readAllBytes();

        try {
            return proxyRestClient.method(HttpMethod.valueOf(request.getMethod()))
                    .uri(targetUrl)
                    .headers(headers -> copyRequestHeaders(request, headers))
                    .body(body)
                    .exchange((clientRequest, clientResponse) -> {
                        byte[] responseBody = clientResponse.getBody().readAllBytes();
                        HttpHeaders responseHeaders = new HttpHeaders();
                        clientResponse.getHeaders().forEach((name, values) -> {
                            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                                responseHeaders.put(name, values);
                            }
                        });
                        return ResponseEntity.status(clientResponse.getStatusCode())
                                .headers(responseHeaders)
                                .body(responseBody);
                    });
        } catch (RestClientException e) {
            log.warn("Echec du forward vers l'amont {} {} : {}", request.getMethod(), targetUrl, e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                    "Service temporairement indisponible");
            problem.setProperty("errorCode", "GATEWAY_UPSTREAM_UNAVAILABLE");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem.toString().getBytes());
        }
    }

    private void copyRequestHeaders(HttpServletRequest request, HttpHeaders target) {
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (HOP_BY_HOP_HEADERS.contains(name.toLowerCase())) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                target.add(name, values.nextElement());
            }
        }
    }

}
