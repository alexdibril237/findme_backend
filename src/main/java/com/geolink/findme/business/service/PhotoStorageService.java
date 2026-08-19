package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.InvalidPhotoException;

import java.util.UUID;

/** Abstraction du stockage des photos d'adresse — permet de remplacer l'implémentation
 *  filesystem par un adaptateur S3-compatible sans toucher à {@link AddressService}. */
public interface PhotoStorageService {

    /** Lève {@link InvalidPhotoException} si type/taille non conformes. */
    void validate(byte[] bytes, String contentType, long sizeBytes);

    /** Stocke le fichier et retourne l'URL publique à persister sur l'adresse. */
    String store(UUID addressId, byte[] bytes, String contentType);
}
