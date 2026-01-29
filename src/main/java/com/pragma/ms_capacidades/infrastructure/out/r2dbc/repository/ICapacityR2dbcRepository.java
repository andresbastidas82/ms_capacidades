package com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository;

import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapacityR2dbcRepository extends ReactiveCrudRepository<CapacityEntity, Long> {

    Mono<Boolean> existsByName(String name);

    @Query("SELECT COUNT(*) FROM capacities")
    Mono<Long> countAll();

    Flux<CapacityEntity> findByIdIn(List<Long> ids);

    /*@Query("""
        SELECT 
            c.id,
            c.name,
            c.description,
            COUNT(ct.technology_id) AS technologyCount
        FROM capacities c
        LEFT JOIN capacity_technology ct 
            ON c.id = ct.capacity_id
        GROUP BY c.id, c.name, c.description
        ORDER BY
            CASE WHEN :sortBy = 'name' THEN c.name END,
            CASE WHEN :sortBy = 'technologyCount' THEN COUNT(ct.technology_id) END
        LIMIT :size OFFSET :offset 
    """)
    Flux<CapacityWithTechCountProjection> findAllWithTechnologyCount(String sortBy, int size, long offset, String direction);*/


}
