package com.geolink.findme.admin.infrastructure.client;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propage l'en-tête Authorization de la requête entrante vers les appels REST sortants
 * (auth-service/address-service revalident indépendamment le même JWT — défense en profondeur,
 * voir docs/conception §7.1). Reste confiné à l'infrastructure : le domaine et l'application
 * n'ont aucune connaissance de HTTP.
 */
final class HeaderPropagation {

    private HeaderPropagation() {
    }

    static String currentAuthorizationHeaderOrNull() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader("Authorization");
    }
}
