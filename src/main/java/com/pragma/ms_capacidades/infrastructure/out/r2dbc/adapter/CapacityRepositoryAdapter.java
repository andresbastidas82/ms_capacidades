package com.pragma.ms_capacidades.infrastructure.out.r2dbc.adapter;

import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.domain.spi.ICapacityPersistencePort;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityTechnologyEntity;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.mapper.ICapacityEntityMapper;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository.ICapacityR2dbcRepository;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository.ICapacityTechnologyR2dbcRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CapacityRepositoryAdapter implements ICapacityPersistencePort {

    private final ICapacityTechnologyR2dbcRepository capacityTechnologyRepository;
    private final ICapacityR2dbcRepository capacityR2dbcRepository;
    private final ICapacityEntityMapper capacityEntityMapper;

    public CapacityRepositoryAdapter(ICapacityTechnologyR2dbcRepository capacityTechnologyRepository,
                                     ICapacityR2dbcRepository capacityR2dbcRepository,
                                     ICapacityEntityMapper capacityEntityMapper) {
        this.capacityTechnologyRepository = capacityTechnologyRepository;
        this.capacityR2dbcRepository = capacityR2dbcRepository;
        this.capacityEntityMapper = capacityEntityMapper;
    }

    @Transactional
    @Override
    public Mono<Capacity> save(Capacity capacity) {
        return Mono.fromSupplier(() -> capacity)
                .map(capacityEntityMapper::toEntity)
                .flatMap(capacityR2dbcRepository::save)
                .flatMap(savedEntity ->
                    Flux.fromIterable(savedEntity.getTechnologyIds())
                        .map(techId -> new CapacityTechnologyEntity(savedEntity.getId(), techId))
                        .flatMap(capacityTechnologyRepository::save)
                        .then(Mono.just(savedEntity))
                )
                .map(capacityEntityMapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return capacityR2dbcRepository.existsByName(name);
    }
}
