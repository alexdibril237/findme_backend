package com.geolink.findme.unit.business.service;
import com.geolink.findme.business.service.ZxingQrCodeService;
import com.geolink.findme.business.service.QrCodeService;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class ZxingQrCodeServiceTest {

    private final QrCodeService service = new ZxingQrCodeService();

    @Test
    void generatePngBase64_retourne_une_chaine_non_vide() {
        String result = service.generatePngBase64("findme:address:1234");

        assertThat(result).isNotBlank();
    }

    @Test
    void generatePngBase64_retourne_du_base64_valide() {
        String result = service.generatePngBase64("findme:address:1234");

        assertThat(Base64.getDecoder().decode(result)).isNotEmpty();
    }

    @Test
    void generatePngBase64_produit_une_image_png_300x300() throws IOException {
        String result = service.generatePngBase64("findme:address:1234");
        byte[] png = Base64.getDecoder().decode(result);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(png));

        assertThat(image.getWidth()).isEqualTo(300);
        assertThat(image.getHeight()).isEqualTo(300);
    }

    @Test
    void generatePngBase64_meme_contenu_donne_la_meme_image() {
        String first = service.generatePngBase64("findme:address:same");
        String second = service.generatePngBase64("findme:address:same");

        assertThat(first).isEqualTo(second);
    }

    @Test
    void generatePngBase64_contenus_differents_donnent_des_images_differentes() {
        String first = service.generatePngBase64("findme:address:one");
        String second = service.generatePngBase64("findme:address:two");

        assertThat(first).isNotEqualTo(second);
    }
}
