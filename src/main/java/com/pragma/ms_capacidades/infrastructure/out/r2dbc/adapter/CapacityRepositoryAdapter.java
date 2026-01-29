package com.pragma.ms_capacidades.infrastructure.out.r2dbc.adapter;

import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.domain.spi.ICapacityPersistencePort;
import com.pragma.ms_capacidades.infrastructure.exception.InvalidSortFieldException;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityTechnologyEntity;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.mapper.ICapacityEntityMapper;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.projections.CapacityWithTechCountProjection;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository.ICapacityR2dbcRepository;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository.ICapacityTechnologyR2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CapacityRepositoryAdapter implements ICapacityPersistencePort {

    private final ICapacityTechnologyR2dbcRepository capacityTechnologyRepository;
    private final ICapacityR2dbcRepository capacityR2dbcRepository;
    private final ICapacityEntityMapper capacityEntityMapper;

    private final DatabaseClient databaseClient;

    public CapacityRepositoryAdapter(ICapacityTechnologyR2dbcRepository capacityTechnologyRepository,
                                     ICapacityR2dbcRepository capacityR2dbcRepository,
                                     ICapacityEntityMapper capacityEntityMapper, DatabaseClient databaseClient) {
        this.capacityTechnologyRepository = capacityTechnologyRepository;
        this.capacityR2dbcRepository = capacityR2dbcRepository;
        this.capacityEntityMapper = capacityEntityMapper;
        this.databaseClient = databaseClient;
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

    @Override
    public Flux<Capacity> findAllPaged(int page, int size, String sortBy, String direction) {
        long offset = (long) page * size;
        String orderBy = resolveOrderBy(sortBy, direction);

        String sql = """
            SELECT 
                c.id,
                c.name,
                c.description,
                COUNT(ct.technology_id) AS technology_count
            FROM capacities c
            LEFT JOIN capacity_technology ct 
                ON c.id = ct.capacity_id
            GROUP BY c.id, c.name, c.description
            ORDER BY %s
            LIMIT %d OFFSET %d
        """.formatted(orderBy, size, offset);

        return databaseClient.sql(sql)
                .map((row, meta) -> new CapacityWithTechCountProjection(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("description", String.class),
                        row.get("technology_count", Long.class)
                ))
                .all()
                .map(capacityEntityMapper::toModelProjection);
    }

    @Override
    public Mono<Long> count() {
        return capacityR2dbcRepository.countAll();
    }

    @Override
    public Flux<Long> findTechnologyIdsByCapacityId(Long capacityId) {
        return capacityTechnologyRepository.findTechnologyIdsByCapacityId(capacityId);
    }

    @Override
    public Flux<Capacity> findCapacitiesByIds(List<Long> ids) {
        return capacityR2dbcRepository.findByIdIn(ids).map(capacityEntityMapper::toModel);
    }

    private String resolveOrderBy(String sortBy, String direction) {
        String column = switch (sortBy.toLowerCase()) {
            case "name" -> "c.name";
            case "technologycount" -> "technology_count";
            default -> throw new InvalidSortFieldException("Invalid sort field. Only 'name' and 'technologyCount' are allowed.");
        };
        String dir = direction.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        return column + " " + dir;
    }

}
