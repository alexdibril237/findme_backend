package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.model.AddressSummary;
import com.geolink.findme.admin.domain.port.AddressServiceClientPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListAddressesUseCase {

    private final AddressServiceClientPort addressServiceClient;

    public ListAddressesUseCase(AddressServiceClientPort addressServiceClient) {
        this.addressServiceClient = addressServiceClient;
    }

    public record Query(Pageable pageable, AddressServiceClientPort.Filters filters) {
    }

    public Page<AddressSummary> execute(Query query) {
        return addressServiceClient.findAddressesPage(query.pageable(), query.filters());
    }
}
