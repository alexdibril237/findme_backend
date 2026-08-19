package com.geolink.findme.presentation.controller;

import com.geolink.findme.business.service.AddressService;
import com.geolink.findme.presentation.dto.AddressSummaryResponse;
import com.geolink.findme.presentation.mapper.AddressWebMapper;
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

    private final AddressService addressService;
    private final AddressWebMapper mapper;

    public AdminAddressController(AddressService addressService, AddressWebMapper mapper) {
        this.addressService = addressService;
        this.mapper = mapper;
    }

    @GetMapping
    public Page<AddressSummaryResponse> list(@RequestParam(required = false) String pays,
                                              @RequestParam(required = false) String ville,
                                              @RequestParam(required = false) String quartier,
                                              Pageable pageable) {
        return addressService.listAddressesForAdmin(pageable, pays, ville, quartier).map(mapper::toSummaryResponse);
    }
}
