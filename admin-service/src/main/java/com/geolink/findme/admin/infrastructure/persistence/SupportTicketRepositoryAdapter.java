package com.geolink.findme.admin.infrastructure.persistence;

import com.geolink.findme.admin.domain.model.SupportTicket;
import com.geolink.findme.admin.domain.model.TicketStatus;
import com.geolink.findme.admin.domain.port.SupportTicketRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SupportTicketRepositoryAdapter implements SupportTicketRepositoryPort {

    private final SupportTicketJpaRepository jpaRepository;

    public SupportTicketRepositoryAdapter(SupportTicketJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SupportTicket save(SupportTicket ticket) {
        return toDomain(jpaRepository.save(toEntity(ticket)));
    }

    @Override
    public Optional<SupportTicket> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<SupportTicket> findPage(Pageable pageable, TicketStatus statusFilter) {
        if (statusFilter == null) {
            return jpaRepository.findAll(pageable).map(this::toDomain);
        }
        return jpaRepository.findByStatus(statusFilter.name(), pageable).map(this::toDomain);
    }

    private SupportTicket toDomain(SupportTicketJpaEntity entity) {
        return SupportTicket.rehydrate(entity.getId(), entity.getName(), entity.getEmail(), entity.getMessage(),
                TicketStatus.valueOf(entity.getStatus()), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private SupportTicketJpaEntity toEntity(SupportTicket ticket) {
        return new SupportTicketJpaEntity(ticket.getId(), ticket.getName(), ticket.getEmail(), ticket.getMessage(),
                ticket.getStatus().name(), ticket.getCreatedAt(), ticket.getUpdatedAt());
    }
}
