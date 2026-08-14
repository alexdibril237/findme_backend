package com.geolink.findme.address.infrastructure.storage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.geolink.findme.address.domain.port.QrCodeGeneratorPort;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

@Component
public class ZxingQrCodeGeneratorAdapter implements QrCodeGeneratorPort {

    private static final int SIZE_PX = 300;

    @Override
    public String generatePngBase64(String content) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, SIZE_PX, SIZE_PX);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (WriterException | IOException e) {
            throw new UncheckedIOException("Échec de la génération du QR code", new IOException(e));
        }
    }
}
