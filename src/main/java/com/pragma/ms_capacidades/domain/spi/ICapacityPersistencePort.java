package com.pragma.ms_capacidades.domain.spi;

import com.pragma.ms_capacidades.domain.model.Capacity;
import reactor.core.publisher.Mono;

public interface ICapacityPersistencePort {
    Mono<Capacity> save(Capacity capacity);

    Mono<Boolean> existsByName(String name);

}
