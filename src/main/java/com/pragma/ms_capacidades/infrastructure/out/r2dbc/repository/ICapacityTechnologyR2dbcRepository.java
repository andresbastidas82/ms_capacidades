package com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository;

import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityTechnologyEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICapacityTechnologyR2dbcRepository extends ReactiveCrudRepository<CapacityTechnologyEntity, Long> {

    @Query("SELECT ct.technology_id FROM capacity_technology ct WHERE ct.capacity_id = :capacityId")
    Flux<Long> findTechnologyIdsByCapacityId(Long capacityId);


    @Query("""
        SELECT DISTINCT ct.technology_id
        FROM capacity_technology ct
        WHERE ct.capacity_id IN (:capacityIds)
          AND NOT EXISTS (
              SELECT 1
              FROM capacity_technology ct2
              WHERE ct2.technology_id = ct.technology_id
                AND ct2.capacity_id NOT IN (:capacityIds)
          )
    """)
    Flux<Long> findTechnologiesNotReferencedInOtherCapacities(@Param("capacityIds") List<Long> capacityIds);


    Mono<Void> deleteAllByCapacityIdIn(List<Long> ids);

}
