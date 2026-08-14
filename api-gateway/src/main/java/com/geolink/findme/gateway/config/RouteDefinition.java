package com.geolink.findme.gateway.config;

import java.util.List;

/** Une entrée par microservice cible : base URL interne + préfixes de chemin qu'elle sert. */
public class RouteDefinition {

    private String baseUrl;
    private List<String> prefixes;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(List<String> prefixes) {
        this.prefixes = prefixes;
    }
}
