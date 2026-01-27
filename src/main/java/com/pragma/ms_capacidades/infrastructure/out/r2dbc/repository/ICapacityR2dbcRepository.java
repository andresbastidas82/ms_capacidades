package com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository;

import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ICapacityR2dbcRepository extends ReactiveCrudRepository<CapacityEntity, Long> {

    Mono<Boolean> existsByName(String name);

}
