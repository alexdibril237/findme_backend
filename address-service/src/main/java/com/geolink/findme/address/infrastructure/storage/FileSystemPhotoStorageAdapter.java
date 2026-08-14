package com.geolink.findme.address.infrastructure.storage;

import com.geolink.findme.address.domain.exception.InvalidPhotoException;
import com.geolink.findme.address.domain.port.PhotoContent;
import com.geolink.findme.address.domain.port.PhotoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
 * Adaptateur "système de fichiers conteneurisé" (cahier des charges §2.2). Le port
 * {@link PhotoStoragePort} permet de le remplacer par un adaptateur S3-compatible sans toucher
 * au domaine ni aux use cases.
 */
@Component
public class FileSystemPhotoStorageAdapter implements PhotoStoragePort {

    private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final Path root;
    private final String publicBaseUrl;
    private final List<String> allowedContentTypes;
    private final long maxFileSizeBytes;

    public FileSystemPhotoStorageAdapter(@Value("${storage.root}") String root,
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
    public void validate(PhotoContent content) {
        if (content == null || content.bytes() == null || content.bytes().length == 0) {
            throw new InvalidPhotoException("Fichier vide ou manquant");
        }
        String contentType = content.contentType() == null ? "" : content.contentType().toLowerCase(Locale.ROOT);
        if (!allowedContentTypes.contains(contentType)) {
            throw new InvalidPhotoException("Type de fichier non autorisé : " + contentType);
        }
        if (content.sizeBytes() > maxFileSizeBytes) {
            throw new InvalidPhotoException("Fichier trop volumineux (max " + maxFileSizeBytes + " octets)");
        }
    }

    @Override
    public String store(UUID addressId, PhotoContent content) {
        String extension = EXTENSION_BY_CONTENT_TYPE.getOrDefault(content.contentType().toLowerCase(Locale.ROOT), "bin");
        String filename = addressId + "-" + UUID.randomUUID() + "." + extension;
        Path target = root.resolve(filename);
        try {
            Files.write(target, content.bytes());
        } catch (IOException e) {
            throw new UncheckedIOException("Échec de l'écriture du fichier photo", e);
        }
        return publicBaseUrl + "/" + filename;
    }
}
