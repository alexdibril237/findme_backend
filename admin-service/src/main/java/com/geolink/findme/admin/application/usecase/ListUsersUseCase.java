package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.model.UserSummary;
import com.geolink.findme.admin.domain.port.AuthServiceClientPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class ListUsersUseCase {

    private final AuthServiceClientPort authServiceClient;

    public ListUsersUseCase(AuthServiceClientPort authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public record Query(String search, Pageable pageable) {
    }

    public Page<UserSummary> execute(Query query) {
        return authServiceClient.findUsersPage(query.search(), query.pageable());
    }
}
