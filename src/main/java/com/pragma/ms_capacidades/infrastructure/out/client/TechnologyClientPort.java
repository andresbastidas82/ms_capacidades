package com.pragma.ms_capacidades.infrastructure.out.client;

import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyClientPort {

    Mono<Boolean> existAllByIds(List<Long> ids);

}
