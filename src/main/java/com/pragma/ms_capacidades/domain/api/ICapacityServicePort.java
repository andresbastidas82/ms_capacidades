package com.pragma.ms_capacidades.domain.api;

import com.pragma.ms_capacidades.domain.model.Capacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICapacityServicePort {
    Mono<Capacity> save(Capacity capacity);

    Flux<Capacity> getCapacities(int page, int size, String sortBy, String direction);

    Mono<Long> count();

}
