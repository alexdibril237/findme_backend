package com.geolink.findme.admin.infrastructure.client;

import com.geolink.findme.admin.domain.exception.UpstreamServiceUnavailableException;
import com.geolink.findme.admin.domain.model.AccountStatus;
import com.geolink.findme.admin.domain.model.Role;
import com.geolink.findme.admin.domain.model.UserSummary;
import com.geolink.findme.admin.domain.port.AuthServiceClientPort;
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
import java.util.UUID;

@Component
public class AuthServiceRestClientAdapter implements AuthServiceClientPort {

    private static final String SERVICE_NAME = "auth-service";

    private final RestClient authServiceRestClient;

    public AuthServiceRestClientAdapter(RestClient authServiceRestClient) {
        this.authServiceRestClient = authServiceRestClient;
    }

    @Override
    public Page<UserSummary> findUsersPage(String search, Pageable pageable) {
        try {
            PageEnvelope<AuthUserDto> page = authServiceRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/internal/users")
                            .queryParamIfPresent("search", java.util.Optional.ofNullable(search))
                            .queryParam("page", pageable.getPageNumber())
                            .queryParam("size", pageable.getPageSize())
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, forwardedAuth())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageEnvelope<AuthUserDto>>() {
                    });

            List<UserSummary> summaries = page.content().stream().map(this::toSummary).toList();
            return new PageImpl<>(summaries, pageable, page.totalElements());
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }

    @Override
    public void updateUserRole(UUID userId, Role newRole) {
        try {
            authServiceRestClient.patch()
                    .uri("/internal/users/{id}/role", userId)
                    .header(HttpHeaders.AUTHORIZATION, forwardedAuth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new RoleUpdateBody(newRole.name()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }

    @Override
    public void updateUserStatus(UUID userId, AccountStatus newStatus) {
        try {
            authServiceRestClient.patch()
                    .uri("/internal/users/{id}/status", userId)
                    .header(HttpHeaders.AUTHORIZATION, forwardedAuth())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new StatusUpdateBody(newStatus.name()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new UpstreamServiceUnavailableException(SERVICE_NAME, e);
        }
    }

    private String forwardedAuth() {
        String header = HeaderPropagation.currentAuthorizationHeaderOrNull();
        return header == null ? "" : header;
    }

    private UserSummary toSummary(AuthUserDto dto) {
        return new UserSummary(dto.id(), dto.email(), dto.prenom(), dto.nom(), Role.valueOf(dto.role()),
                dto.statutCompte(), dto.dateCreation(), dto.dateDerniereConnexion());
    }

    private record RoleUpdateBody(String role) {
    }

    private record StatusUpdateBody(String statut) {
    }
}
