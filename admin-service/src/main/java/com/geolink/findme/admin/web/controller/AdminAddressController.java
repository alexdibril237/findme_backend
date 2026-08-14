package com.geolink.findme.admin.web.controller;

import com.geolink.findme.admin.application.usecase.ListAddressesUseCase;
import com.geolink.findme.admin.domain.port.AddressServiceClientPort;
import com.geolink.findme.admin.web.dto.AddressSummaryResponse;
import com.geolink.findme.admin.web.mapper.AdminWebMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Réservé ADMIN (cahier des charges §2.3, matrice RBAC §2.4). */
@RestController
@RequestMapping("/api/admin/addresses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAddressController {

    private final ListAddressesUseCase listAddressesUseCase;
    private final AdminWebMapper mapper;

    public AdminAddressController(ListAddressesUseCase listAddressesUseCase, AdminWebMapper mapper) {
        this.listAddressesUseCase = listAddressesUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<AddressSummaryResponse> list(@RequestParam(required = false) String pays,
                                              @RequestParam(required = false) String ville,
                                              @RequestParam(required = false) String quartier,
                                              Pageable pageable) {
        var filters = new AddressServiceClientPort.Filters(pays, ville, quartier);
        return listAddressesUseCase.execute(new ListAddressesUseCase.Query(pageable, filters)).map(mapper::toResponse);
    }
}
