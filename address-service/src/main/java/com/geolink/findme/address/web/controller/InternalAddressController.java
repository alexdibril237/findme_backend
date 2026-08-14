package com.geolink.findme.address.web.controller;

import com.geolink.findme.address.application.usecase.ListAddressesForAdminUseCase;
import com.geolink.findme.address.domain.port.AddressRepositoryPort.AddressFilters;
import com.geolink.findme.address.web.dto.AddressResponse;
import com.geolink.findme.address.web.mapper.AddressWebMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consommé uniquement par admin-service — jamais exposé par l'API Gateway publique. */
@RestController
@RequestMapping("/internal/addresses")
@PreAuthorize("hasRole('ADMIN')")
public class InternalAddressController {

    private final ListAddressesForAdminUseCase listAddressesForAdminUseCase;
    private final AddressWebMapper mapper;

    public InternalAddressController(ListAddressesForAdminUseCase listAddressesForAdminUseCase, AddressWebMapper mapper) {
        this.listAddressesForAdminUseCase = listAddressesForAdminUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<AddressResponse> list(@RequestParam(required = false) String pays,
                                       @RequestParam(required = false) String ville,
                                       @RequestParam(required = false) String quartier,
                                       Pageable pageable) {
        var query = new ListAddressesForAdminUseCase.Query(pageable, new AddressFilters(pays, ville, quartier));
        return listAddressesForAdminUseCase.execute(query).map(mapper::toResponse);
    }
}
