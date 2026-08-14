package com.geolink.findme.address.domain.port;

public record PhotoContent(byte[] bytes, String contentType, long sizeBytes, String originalFilename) {
}
