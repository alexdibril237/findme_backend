package com.geolink.findme.admin.domain.port;

import com.geolink.findme.admin.domain.model.AddressSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AddressServiceClientPort {

    record Filters(String pays, String ville, String quartier) {
        public static Filters none() {
            return new Filters(null, null, null);
        }
    }

    /** Lève {@link com.geolink.findme.admin.domain.exception.UpstreamServiceUnavailableException} si address-service ne répond pas. */
    Page<AddressSummary> findAddressesPage(Pageable pageable, Filters filters);
}
