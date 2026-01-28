package com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository;

import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityTechnologyEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ICapacityTechnologyR2dbcRepository extends ReactiveCrudRepository<CapacityTechnologyEntity, Long> {

    @Query("SELECT ct.technology_id FROM capacity_technology ct WHERE ct.capacity_id = :capacityId")
    Flux<Long> findTechnologyIdsByCapacityId(Long capacityId);
}
