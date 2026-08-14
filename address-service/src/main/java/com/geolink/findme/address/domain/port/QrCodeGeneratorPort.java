package com.geolink.findme.address.domain.port;

public interface QrCodeGeneratorPort {

    /** Retourne une image PNG encodée en base64 (sans préfixe data URI) représentant le contenu donné. */
    String generatePngBase64(String content);
}
