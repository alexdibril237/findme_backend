package com.geolink.findme.admin.infrastructure.client;

import com.geolink.findme.admin.domain.exception.UpstreamServiceUnavailableException;
import com.geolink.findme.admin.domain.model.AddressSummary;
import com.geolink.findme.admin.domain.port.AddressServiceClientPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

@Component
public class AddressServiceRestClientAdapter implements AddressServiceClientPort {

    private static final String SERVICE_NAME = "address-service";

    private final RestClient addressServiceRestClient;

    public AddressServiceRestClientAdapter(RestClient addressServiceRestClient) {
        this.addressServiceRestClient = addressServiceRestClient;
    }

    @Override
    public Page<AddressSummary> findAddressesPage(Pageable pageable, Filters filters) {
        try {
            PageEnvelope<AddressDto> page = addressServiceRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/addresses")
                            .queryParamIfPresent("pays", Optional.ofNullable(filters.pays()))
                            .queryParamIfPresent("ville", Optional.ofNullable(filters.ville()))
                            .queryParamIfPresent("quartier", Optional.ofNullable(filters.quartier()))
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, forwardedAuth())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageEnvelope<AddressDto>>() {
                    });

            List<AddressSummary> summaries = page.content().stream().map(this::toSummary).toList();
            return new PageImpl<>(summaries, pageable, page.totalElements());
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }

    private String forwardedAuth() {
        String header = HeaderPropagation.currentAuthorizationHeaderOrNull();
        return header == null ? "" : header;
    }

    private AddressSummary toSummary(AddressDto dto) {
        return new AddressSummary(dto.id(), dto.userId(), dto.pays(), dto.ville(), dto.quartier(), dto.rue(),
                dto.numero(), dto.dateCreation());
    }
}
