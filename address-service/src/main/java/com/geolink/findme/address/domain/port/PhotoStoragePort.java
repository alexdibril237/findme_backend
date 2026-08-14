package com.geolink.findme.address.domain.port;

import java.util.UUID;

public interface PhotoStoragePort {

    /** Lève {@link com.geolink.findme.address.domain.exception.InvalidPhotoException} si type/taille non conformes. */
    void validate(PhotoContent content);

    /** Stocke le fichier et retourne l'URL publique à persister sur l'adresse. */
    String store(UUID addressId, PhotoContent content);
}
