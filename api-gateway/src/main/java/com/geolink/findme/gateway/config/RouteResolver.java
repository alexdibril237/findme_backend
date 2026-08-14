package com.geolink.findme.gateway.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RouteResolver {

    private final Map<String, RouteDefinition> routes;

    public RouteResolver(Map<String, RouteDefinition> routes) {
        this.routes = routes;
    }

    public Optional<RouteDefinition> resolve(String path) {
        return routes.values().stream()
                .filter(route -> route.getPrefixes() != null
                        && route.getPrefixes().stream().anyMatch(path::startsWith))
                .findFirst();
    }
}
