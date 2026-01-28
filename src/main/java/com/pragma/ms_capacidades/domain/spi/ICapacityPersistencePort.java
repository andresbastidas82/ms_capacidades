package com.pragma.ms_capacidades.domain.spi;

import com.pragma.ms_capacidades.domain.model.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICapacityPersistencePort {
    Mono<Capacity> save(Capacity capacity);

    Mono<Boolean> existsByName(String name);

    Flux<Capacity> findAllPaged(int page, int size, String sortBy, String direction);

    Mono<Long> count();

    Flux<Long> findTechnologyIdsByCapacityId(Long capacityId);

}
