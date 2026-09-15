package com.geolink.findme.unit.business.service;
import com.geolink.findme.business.service.FileSystemPhotoStorageService;

import com.geolink.findme.business.exception.InvalidPhotoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileSystemPhotoStorageServiceTest {

    private static final String ALLOWED_TYPES = "image/jpeg,image/png,image/webp";
    private static final long MAX_SIZE = 1024;

    private Path root;
    private FileSystemPhotoStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        root = Files.createTempDirectory("findme-photo-test");
        service = new FileSystemPhotoStorageService(root.toString(), "http://files", ALLOWED_TYPES, MAX_SIZE);
    }

    // --- validate ---

    @Test
    void validate_refuse_des_bytes_vides() {
        assertThatThrownBy(() -> service.validate(new byte[0], "image/jpeg", 0))
                .isInstanceOf(InvalidPhotoException.class);
    }

    @Test
    void validate_refuse_des_bytes_null() {
        assertThatThrownBy(() -> service.validate(null, "image/jpeg", 0))
                .isInstanceOf(InvalidPhotoException.class);
    }

    @Test
    void validate_refuse_un_type_non_autorise() {
        assertThatThrownBy(() -> service.validate(new byte[]{1}, "image/gif", 1))
                .isInstanceOf(InvalidPhotoException.class);
    }

    @Test
    void validate_refuse_un_fichier_trop_volumineux() {
        assertThatThrownBy(() -> service.validate(new byte[]{1}, "image/jpeg", MAX_SIZE + 1))
                .isInstanceOf(InvalidPhotoException.class);
    }

    @Test
    void validate_accepte_un_fichier_conforme() {
        assertThatCode(() -> service.validate(new byte[]{1, 2, 3}, "image/jpeg", 3)).doesNotThrowAnyException();
    }

    @Test
    void validate_accepte_le_type_en_majuscules() {
        assertThatCode(() -> service.validate(new byte[]{1}, "IMAGE/PNG", 1)).doesNotThrowAnyException();
    }

    // --- store ---

    @Test
    void store_genere_une_url_avec_extension_jpg() {
        String url = service.store(java.util.UUID.randomUUID(), new byte[]{1, 2}, "image/jpeg");
        assertThat(url).startsWith("http://files/").endsWith(".jpg");
    }

    @Test
    void store_genere_une_url_avec_extension_png() {
        String url = service.store(java.util.UUID.randomUUID(), new byte[]{1, 2}, "image/png");
        assertThat(url).endsWith(".png");
    }

    @Test
    void store_genere_une_url_avec_extension_webp() {
        String url = service.store(java.util.UUID.randomUUID(), new byte[]{1, 2}, "image/webp");
        assertThat(url).endsWith(".webp");
    }

    @Test
    void store_utilise_bin_pour_un_type_inconnu() {
        String url = service.store(java.util.UUID.randomUUID(), new byte[]{1, 2}, "application/octet-stream");
        assertThat(url).endsWith(".bin");
    }

    @Test
    void store_ecrit_reellement_le_fichier_sur_le_disque() throws IOException {
        byte[] content = {10, 20, 30};
        String url = service.store(java.util.UUID.randomUUID(), content, "image/jpeg");
        String filename = url.substring(url.lastIndexOf('/') + 1);

        byte[] written = Files.readAllBytes(root.resolve(filename));

        assertThat(written).isEqualTo(content);
    }
}
