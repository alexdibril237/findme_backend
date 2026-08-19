package com.geolink.findme.business.service;

/** Abstraction de la génération de QR code — permet de remplacer ZXing par une autre librairie
 *  sans toucher à {@link AddressService}. */
public interface QrCodeService {

    /** Retourne une image PNG encodée en base64 (sans préfixe data URI) représentant le contenu donné. */
    String generatePngBase64(String content);
}
