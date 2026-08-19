package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.InvalidPhotoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptateur "système de fichiers conteneurisé" (cahier des charges §2.2) : stocke les photos
 * d'adresses sur le disque local du conteneur et les sert publiquement via {@code /files/**}
 * (voir {@link com.geolink.findme.config.StaticFilesConfig}).
 */
@Service
public class FileSystemPhotoStorageService implements PhotoStorageService {

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path root;
    private final String publicBaseUrl;
    private final List<String> allowedContentTypes;
    private final long maxFileSizeBytes;

    public FileSystemPhotoStorageService(@Value("${storage.root}") String root,
                                          @Value("${storage.public-base-url}") String publicBaseUrl,
                                          @Value("${storage.allowed-content-types}") String allowedContentTypes,
                                          @Value("${storage.max-file-size-bytes}") long maxFileSizeBytes) {
        this.root = Path.of(root);
        this.publicBaseUrl = publicBaseUrl;
        this.allowedContentTypes = Arrays.asList(allowedContentTypes.split(","));
        this.maxFileSizeBytes = maxFileSizeBytes;
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'initialiser le répertoire de stockage : " + root, e);
        }
    }

    @Override
    public void validate(byte[] bytes, String contentType, long sizeBytes) {
        if (bytes == null || bytes.length == 0) {
            throw new InvalidPhotoException("Fichier vide ou manquant");
        }
        String normalizedContentType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        if (!allowedContentTypes.contains(normalizedContentType)) {
            throw new InvalidPhotoException("Type de fichier non autorisé : " + normalizedContentType);
        }
        if (sizeBytes > maxFileSizeBytes) {
            throw new InvalidPhotoException("Fichier trop volumineux (max " + maxFileSizeBytes + " octets)");
        }
    }

    @Override
    public String store(UUID addressId, byte[] bytes, String contentType) {
        String extension = EXTENSION_BY_CONTENT_TYPE.getOrDefault(contentType.toLowerCase(Locale.ROOT), "bin");
        String filename = addressId + "-" + UUID.randomUUID() + "." + extension;
        Path target = root.resolve(filename);
        try {
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException("Échec de l'écriture du fichier photo", e);
        }
        return publicBaseUrl + "/" + filename;
    }
}
