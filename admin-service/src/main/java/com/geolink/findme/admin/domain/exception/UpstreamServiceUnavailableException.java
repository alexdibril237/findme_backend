package com.geolink.findme.admin.domain.exception;

/**
 * Levée quand auth-service ou address-service est indisponible/en timeout lors d'un appel REST
 * synchrone (cahier des charges §2.3 : "toute indisponibilité doit être gérée proprement").
 */
public class UpstreamServiceUnavailableException extends RuntimeException {

    public UpstreamServiceUnavailableException(String serviceName, Throwable cause) {
        super("Service indisponible : " + serviceName, cause);
    }
}
