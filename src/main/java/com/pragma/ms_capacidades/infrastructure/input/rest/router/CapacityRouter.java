package com.pragma.ms_capacidades.infrastructure.input.rest.router;

import com.pragma.ms_capacidades.infrastructure.input.rest.handler.CapacityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class CapacityRouter {

    @Bean
    public RouterFunction<ServerResponse> capacityRoutes(CapacityHandler handler) {

        return route(POST("/api/v1/capacity"), handler::createCapacity)
                .andRoute(GET("/api/v1/capacity"), handler::listCapacities)
                .andRoute(GET("/api/v1/capacity/byIds"), handler::getCapacitiesByIds)
                .andRoute(DELETE("/api/v1/capacity"), handler::deleteCapacities);
    }
}
