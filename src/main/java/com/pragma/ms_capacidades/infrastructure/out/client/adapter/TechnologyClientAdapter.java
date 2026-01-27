package com.pragma.ms_capacidades.infrastructure.out.client.adapter;

import com.pragma.ms_capacidades.infrastructure.out.client.TechnologyClientPort;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class TechnologyClientAdapter implements TechnologyClientPort {

    private final WebClient webClient;

    public TechnologyClientAdapter(@Qualifier("technologyWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Boolean> existAllByIds(List<Long> ids) {
        return webClient.post()
                .uri("/technology/validate")
                .bodyValue(ids)
                .retrieve()
                .bodyToMono(Boolean.class);
    }
}
