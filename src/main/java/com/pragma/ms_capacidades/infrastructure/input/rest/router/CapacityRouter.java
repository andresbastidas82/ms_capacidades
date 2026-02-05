package com.pragma.ms_capacidades.infrastructure.input.rest.router;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.infrastructure.input.rest.handler.CapacityHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class CapacityRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/capacity",
                    method = RequestMethod.POST,
                    beanClass = CapacityHandler.class,
                    beanMethod = "createCapacity",
                    operation = @Operation(
                            operationId = "createCapacity",
                            summary = "Registrar una capacidad",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = CapacityRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Capacidad creada")
                            }
                    )
            ),

            @RouterOperation(
                    path = "/api/v1/capacity",
                    method = RequestMethod.GET,
                    beanClass = CapacityHandler.class,
                    beanMethod = "listCapacities",
                    operation = @Operation(
                            operationId = "listCapacities",
                            summary = "Listar capacidades",
                            parameters = {
                                    @Parameter(name = "page", description = "Numero de la pagina"),
                                    @Parameter(name = "size", description = "Cantidad de registros por pagina"),
                                    @Parameter(name = "direction", description = "Orden de los registros"),
                                    @Parameter(name = "sortBy", description = "Ordenar por")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Lista de capacidades")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/capacity/byIds",
                    method = RequestMethod.GET,
                    beanClass = CapacityHandler.class,
                    beanMethod = "getCapacitiesByIds",
                    operation = @Operation(
                            operationId = "getCapacitiesByIds",
                            summary = "Obtener capacidades por ids",
                            parameters = {
                                    @Parameter(name = "ids", description = "Lista de ids de capacidades", required = true)
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Lista de capacidades")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/capacity",
                    method = RequestMethod.DELETE,
                    beanClass = CapacityHandler.class,
                    beanMethod = "deleteCapacities",
                    operation = @Operation(
                            operationId = "deleteCapacities",
                            summary = "Eliminar capacidades por ids",
                            parameters = {
                                    @Parameter(name = "ids", description = "Lista de ids de capacidades", required = true)
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Capacidades eliminadas")
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> capacityRoutes(CapacityHandler handler) {

        return route(POST("/api/v1/capacity"), handler::createCapacity)
                .andRoute(GET("/api/v1/capacity"), handler::listCapacities)
                .andRoute(GET("/api/v1/capacity/byIds"), handler::getCapacitiesByIds)
                .andRoute(DELETE("/api/v1/capacity"), handler::deleteCapacities);
    }
}
