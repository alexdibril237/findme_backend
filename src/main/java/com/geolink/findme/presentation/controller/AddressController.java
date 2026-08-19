package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.exception.InvalidPhotoException;
import com.geolink.findme.business.service.AddressService;
import com.geolink.findme.presentation.dto.AddressExportResponse;
import com.geolink.findme.presentation.dto.AddressRequest;
import com.geolink.findme.presentation.dto.AddressResponse;
import com.geolink.findme.presentation.mapper.AddressWebMapper;
import com.geolink.findme.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final CurrentUserProvider currentUserProvider;
    private final AddressWebMapper mapper;

    public AddressController(AddressService addressService, CurrentUserProvider currentUserProvider,
                              AddressWebMapper mapper) {
        this.addressService = addressService;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<AddressResponse> list(@RequestParam(required = false) String pays,
                                       @RequestParam(required = false) String ville,
                                       @RequestParam(required = false) String quartier,
                                       Pageable pageable) {
        var addresses = addressService.listMyAddresses(currentUserProvider.requireCurrentUserId(), pageable,
                pays, ville, quartier);
        return addresses.map(mapper::toResponse);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        var address = addressService.createAddress(currentUserProvider.requireCurrentUserId(), request.pays(),
                request.ville(), request.quartier(), request.rue(), request.numero(), request.codePostal(),
                request.latitude(), request.longitude());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(address));
    }

    @GetMapping("/{id}")
    public AddressResponse get(@PathVariable UUID id) {
        var address = addressService.getAddress(id, currentUserProvider.requireCurrentUserId());
        return mapper.toResponse(address);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@PathVariable UUID id, @Valid @RequestBody AddressRequest request) {
        var address = addressService.updateAddress(id, currentUserProvider.requireCurrentUserId(), request.pays(),
                request.ville(), request.quartier(), request.rue(), request.numero(), request.codePostal(),
                request.latitude(), request.longitude());
        return mapper.toResponse(address);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        addressService.deleteAddress(id, currentUserProvider.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photo")
    public AddressResponse uploadPhoto(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidPhotoException("Fichier illisible");
        }
        var address = addressService.uploadPhoto(id, currentUserProvider.requireCurrentUserId(), bytes,
                file.getContentType(), file.getSize());
        return mapper.toResponse(address);
    }

    @GetMapping("/{id}/export")
    public AddressExportResponse export(@PathVariable UUID id) {
        var data = addressService.exportAddress(id, currentUserProvider.requireCurrentUserId());
        return mapper.toExportResponse(data);
    }
}
