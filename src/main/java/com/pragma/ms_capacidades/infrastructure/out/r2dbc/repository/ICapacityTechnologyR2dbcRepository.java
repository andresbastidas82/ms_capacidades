package com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository;

import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityTechnologyEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ICapacityTechnologyR2dbcRepository extends ReactiveCrudRepository<CapacityTechnologyEntity, Long> {
}
