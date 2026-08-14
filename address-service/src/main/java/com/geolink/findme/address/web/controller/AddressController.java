package com.geolink.findme.address.web.controller;

import com.geolink.findme.address.application.usecase.CreateAddressUseCase;
import com.geolink.findme.address.application.usecase.DeleteAddressUseCase;
import com.geolink.findme.address.application.usecase.ExportAddressUseCase;
import com.geolink.findme.address.application.usecase.GetAddressUseCase;
import com.geolink.findme.address.application.usecase.ListMyAddressesUseCase;
import com.geolink.findme.address.application.usecase.UpdateAddressUseCase;
import com.geolink.findme.address.application.usecase.UploadAddressPhotoUseCase;
import com.geolink.findme.address.domain.exception.InvalidPhotoException;
import com.geolink.findme.address.domain.port.AddressRepositoryPort.AddressFilters;
import com.geolink.findme.address.domain.port.PhotoContent;
import com.geolink.findme.address.infrastructure.security.CurrentUserProvider;
import com.geolink.findme.address.web.dto.AddressExportResponse;
import com.geolink.findme.address.web.dto.AddressRequest;
import com.geolink.findme.address.web.dto.AddressResponse;
import com.geolink.findme.address.web.mapper.AddressWebMapper;
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

    private final CreateAddressUseCase createAddressUseCase;
    private final ListMyAddressesUseCase listMyAddressesUseCase;
    private final GetAddressUseCase getAddressUseCase;
    private final UpdateAddressUseCase updateAddressUseCase;
    private final DeleteAddressUseCase deleteAddressUseCase;
    private final UploadAddressPhotoUseCase uploadAddressPhotoUseCase;
    private final ExportAddressUseCase exportAddressUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final AddressWebMapper mapper;

    public AddressController(CreateAddressUseCase createAddressUseCase, ListMyAddressesUseCase listMyAddressesUseCase,
                              GetAddressUseCase getAddressUseCase, UpdateAddressUseCase updateAddressUseCase,
                              DeleteAddressUseCase deleteAddressUseCase, UploadAddressPhotoUseCase uploadAddressPhotoUseCase,
                              ExportAddressUseCase exportAddressUseCase, CurrentUserProvider currentUserProvider,
                              AddressWebMapper mapper) {
        this.createAddressUseCase = createAddressUseCase;
        this.listMyAddressesUseCase = listMyAddressesUseCase;
        this.getAddressUseCase = getAddressUseCase;
        this.updateAddressUseCase = updateAddressUseCase;
        this.deleteAddressUseCase = deleteAddressUseCase;
        this.uploadAddressPhotoUseCase = uploadAddressPhotoUseCase;
        this.exportAddressUseCase = exportAddressUseCase;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<AddressResponse> list(@RequestParam(required = false) String pays,
                                       @RequestParam(required = false) String ville,
                                       @RequestParam(required = false) String quartier,
                                       Pageable pageable) {
        var query = new ListMyAddressesUseCase.Query(currentUserProvider.requireCurrentUserId(), pageable,
                new AddressFilters(pays, ville, quartier));
        return listMyAddressesUseCase.execute(query).map(mapper::toResponse);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressRequest request) {
        var address = createAddressUseCase.execute(new CreateAddressUseCase.Command(
                currentUserProvider.requireCurrentUserId(), request.pays(), request.ville(), request.quartier(),
                request.rue(), request.numero(), request.codePostal(), request.latitude(), request.longitude()));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(address));
    }

    @GetMapping("/{id}")
    public AddressResponse get(@PathVariable UUID id) {
        var address = getAddressUseCase.execute(id, currentUserProvider.requireCurrentUserId());
        return mapper.toResponse(address);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@PathVariable UUID id, @Valid @RequestBody AddressRequest request) {
        var address = updateAddressUseCase.execute(new UpdateAddressUseCase.Command(id,
                currentUserProvider.requireCurrentUserId(), request.pays(), request.ville(), request.quartier(),
                request.rue(), request.numero(), request.codePostal(), request.latitude(), request.longitude()));
        return mapper.toResponse(address);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteAddressUseCase.execute(id, currentUserProvider.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photo")
    public AddressResponse uploadPhoto(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        PhotoContent content;
        try {
            content = new PhotoContent(file.getBytes(), file.getContentType(), file.getSize(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new InvalidPhotoException("Fichier illisible");
        }
        var address = uploadAddressPhotoUseCase.execute(new UploadAddressPhotoUseCase.Command(id,
                currentUserProvider.requireCurrentUserId(), content));
        return mapper.toResponse(address);
    }

    @GetMapping("/{id}/export")
    public AddressExportResponse export(@PathVariable UUID id) {
        var data = exportAddressUseCase.execute(id, currentUserProvider.requireCurrentUserId());
        return mapper.toExportResponse(data);
    }
}
