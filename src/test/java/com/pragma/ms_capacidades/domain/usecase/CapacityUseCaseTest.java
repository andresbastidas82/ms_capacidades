package com.pragma.ms_capacidades.domain.usecase;

import com.pragma.ms_capacidades.domain.exception.BadRequestException;
import com.pragma.ms_capacidades.domain.exception.CapacityAlreadyExistsException;
import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.domain.spi.ICapacityPersistencePort;
import com.pragma.ms_capacidades.domain.spi.TechnologyClientPort;
import com.pragma.ms_capacidades.infrastructure.input.rest.dto.TechnologyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityUseCaseTest {

    @Mock
    private ICapacityPersistencePort capacityPersistencePort;

    @Mock
    private TechnologyClientPort technologyClientPort;

    @InjectMocks
    private CapacityUseCase capacityUseCase;

    @Test
    @DisplayName("Save: Should save capacity when all rules are met (Happy Path)")
    void save_WhenAllRulesMet_ShouldReturnCapacity() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capacity capacity = new Capacity(null, "Fullstack Java", "Descripción", techIds, null, null);

        // Simulamos que las tecnologías existen externamente
        when(technologyClientPort.existAllByIds(techIds)).thenReturn(Mono.just(true));
        // Simulamos que el nombre NO existe en BD (gracias al Mono.defer esto se llama en el momento correcto)
        when(capacityPersistencePort.existsByName(capacity.getName())).thenReturn(Mono.just(false));
        // Simulamos el guardado
        when(capacityPersistencePort.save(capacity)).thenReturn(Mono.just(capacity));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectNext(capacity)
                .verifyComplete();

        verify(technologyClientPort).existAllByIds(techIds);
        verify(capacityPersistencePort).existsByName(capacity.getName());
        verify(capacityPersistencePort).save(capacity);
    }

    @Test
    @DisplayName("Save: Should throw BadRequestException when multiple validations fail")
    void save_WhenMultipleValidationsFail_ShouldThrowBadRequestException() {
        // Arrange
        // Caso: Nombre vacío Y lista de tecnologías vacía (menos de 3)
        Capacity capacity = new Capacity(null, "", "Desc", Collections.emptyList(), null, null);

        // Nota: Tu código llama a existAllByIds incluso si la lista está vacía, así que debemos mockearlo
        // para evitar NullPointerException o errores inesperados en el test.
        when(technologyClientPort.existAllByIds(anyList())).thenReturn(Mono.just(true));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof BadRequestException &&
                        throwable.getMessage().contains("|")) // Verifica que concatena errores
                .verify();

        verifyNoInteractions(capacityPersistencePort); // No debe intentar guardar
    }

    @Test
    @DisplayName("Save: Should throw BadRequestException when technologies do not exist")
    void save_WhenTechnologiesDoNotExist_ShouldThrowBadRequestException() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capacity capacity = new Capacity(null, "Java Dev", "Desc", techIds, null, null);

        // Simulamos que el microservicio de tecnologías devuelve FALSE
        when(technologyClientPort.existAllByIds(techIds)).thenReturn(Mono.just(false));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();

        verify(technologyClientPort).existAllByIds(techIds);
        verifyNoInteractions(capacityPersistencePort);
    }

    @Test
    @DisplayName("Save: Should throw CapacityAlreadyExistsException when name exists in DB")
    void save_WhenNameExists_ShouldThrowCapacityAlreadyExistsException() {
        // Arrange
        List<Long> techIds = Arrays.asList(1L, 2L, 3L);
        Capacity capacity = new Capacity(null, "Existing Name", "Desc", techIds, null, null);

        when(technologyClientPort.existAllByIds(techIds)).thenReturn(Mono.just(true));
        // Simulamos que el nombre YA existe
        when(capacityPersistencePort.existsByName(capacity.getName())).thenReturn(Mono.just(true));

        // Act
        Mono<Capacity> result = capacityUseCase.save(capacity);

        // Assert
        StepVerifier.create(result)
                .expectError(CapacityAlreadyExistsException.class)
                .verify();

        verify(capacityPersistencePort, never()).save(any());
    }


    @Test
    @DisplayName("GetCapacities: Should return enriched capacities with technologies")
    void getCapacities_ShouldReturnEnrichedCapacities() {
        // Arrange
        int page = 0, size = 10;
        String sort = "name", direction = "asc";
        Long capId = 1L;
        List<Long> techIds = Arrays.asList(10L, 20L);

        Capacity capacity = new Capacity(capId, "Java", "Desc", null, null, null);

        // Mock de TechnologyResponse (DTO externo)
        TechnologyResponse techResp1 = new TechnologyResponse(10L, "Java", "Desc");
        TechnologyResponse techResp2 = new TechnologyResponse(20L, "Spring", "Desc");

        // 1. Mockear la búsqueda paginada
        when(capacityPersistencePort.findAllPaged(page, size, sort, direction))
                .thenReturn(Flux.just(capacity));

        // 2. Mockear la búsqueda de IDs de tecnologías para esa capacidad
        when(capacityPersistencePort.findTechnologyIdsByCapacityId(capId))
                .thenReturn(Flux.fromIterable(techIds));

        // 3. Mockear la llamada al cliente externo para obtener detalles de tecnologías
        when(technologyClientPort.getTechnologiesByIds(techIds))
                .thenReturn(Flux.just(techResp1, techResp2));

        // Act
        Flux<Capacity> result = capacityUseCase.getCapacities(page, size, sort, direction);

        // Assert
        StepVerifier.create(result)
                .assertNext(cap -> {
                    // Verificamos que se haya enriquecido el objeto
                    assert cap.getTechnologies() != null;
                    assert cap.getTechnologies().size() == 2;
                    assert cap.getTechnologyCount() == 2;
                    assert cap.getTechnologies().get(0).getName().equals("Java");
                })
                .verifyComplete();
    }


    @Test
    @DisplayName("Count: Should return total number of capacities")
    void count_ShouldReturnTotal() {
        // Arrange
        when(capacityPersistencePort.count()).thenReturn(Mono.just(5L));

        // Act
        Mono<Long> result = capacityUseCase.count();

        // Assert
        StepVerifier.create(result)
                .expectNext(5L)
                .verifyComplete();
    }


    @Test
    @DisplayName("GetCapacitiesByIds: Should return enriched capacities")
    void getCapacitiesByIds_ShouldReturnEnrichedCapacities() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);
        Capacity capacity1 = new Capacity(1L, "Cap 1", "Desc 1", null, null, null);
        Capacity capacity2 = new Capacity(2L, "Cap 2", "Desc 2", null, null, null);

        // Datos para el enriquecimiento (simulando getCapacityWhitTechnologies)
        List<Long> techIds1 = Arrays.asList(10L);
        List<Long> techIds2 = Arrays.asList(20L);
        TechnologyResponse techResp1 = new TechnologyResponse(10L, "Java", "Desc");
        TechnologyResponse techResp2 = new TechnologyResponse(20L, "Python", "Desc");

        // 1. Mockear la búsqueda de capacidades por IDs
        when(capacityPersistencePort.findCapacitiesByIds(ids))
                .thenReturn(Flux.just(capacity1, capacity2));

        // 2. Mockear la búsqueda de IDs de tecnologías (se llama por cada capacidad)
        when(capacityPersistencePort.findTechnologyIdsByCapacityId(1L)).thenReturn(Flux.fromIterable(techIds1));
        when(capacityPersistencePort.findTechnologyIdsByCapacityId(2L)).thenReturn(Flux.fromIterable(techIds2));

        // 3. Mockear la llamada al cliente externo
        when(technologyClientPort.getTechnologiesByIds(techIds1)).thenReturn(Flux.just(techResp1));
        when(technologyClientPort.getTechnologiesByIds(techIds2)).thenReturn(Flux.just(techResp2));

        // Act
        Flux<Capacity> result = capacityUseCase.getCapacitiesByIds(ids);

        // Assert
        StepVerifier.create(result)
                .assertNext(cap -> {
                    assert cap.getId().equals(1L);
                    assert cap.getTechnologies().get(0).getName().equals("Java");
                })
                .assertNext(cap -> {
                    assert cap.getId().equals(2L);
                    assert cap.getTechnologies().get(0).getName().equals("Python");
                })
                .verifyComplete();

        verify(capacityPersistencePort).findCapacitiesByIds(ids);
    }

// --- TESTS PARA deleteCapacities ---

    @Test
    @DisplayName("DeleteCapacities: Should delete technologies and capacities when orphans exist")
    void deleteCapacities_WhenOrphansExist_ShouldDeleteBoth() {
        // Arrange
        List<Long> capacityIds = Arrays.asList(1L, 2L);
        List<Long> orphanedTechIds = Arrays.asList(100L, 101L);

        // 1. Simular que existen tecnologías que quedarían huérfanas
        when(capacityPersistencePort.findTechnologiesNotReferencedInOtherCapacities(capacityIds))
                .thenReturn(Flux.fromIterable(orphanedTechIds));

        // 2. Simular el borrado exitoso de esas tecnologías
        when(technologyClientPort.deleteTechnolgies(orphanedTechIds))
                .thenReturn(Mono.just(true));

        // 3. Simular el borrado de las capacidades
        when(capacityPersistencePort.deleteCapacities(capacityIds))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = capacityUseCase.deleteCapacities(capacityIds);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        // Verificaciones
        verify(technologyClientPort).deleteTechnolgies(orphanedTechIds); // Se debió llamar
        verify(capacityPersistencePort).deleteCapacities(capacityIds);   // Se debió llamar
    }

    @Test
    @DisplayName("DeleteCapacities: Should only delete capacities when no orphans exist")
    void deleteCapacities_WhenNoOrphans_ShouldSkipTechDelete() {
        // Arrange
        List<Long> capacityIds = Arrays.asList(1L, 2L);

        // 1. Simular que NO hay tecnologías huérfanas (Flux vacío)
        when(capacityPersistencePort.findTechnologiesNotReferencedInOtherCapacities(capacityIds))
                .thenReturn(Flux.empty());

        // 2. Simular el borrado de las capacidades
        when(capacityPersistencePort.deleteCapacities(capacityIds))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = capacityUseCase.deleteCapacities(capacityIds);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        // Verificaciones Clave
        verify(technologyClientPort, never()).deleteTechnolgies(anyList()); // NO se debió llamar
        verify(capacityPersistencePort).deleteCapacities(capacityIds);
    }

    @Test
    @DisplayName("DeleteCapacities: Should return false if technology deletion fails")
    void deleteCapacities_WhenTechDeleteFails_ShouldReturnFalse() {
        // Arrange
        List<Long> capacityIds = Arrays.asList(1L);
        List<Long> orphanedTechIds = Arrays.asList(100L);

        // 1. Hay huérfanas
        when(capacityPersistencePort.findTechnologiesNotReferencedInOtherCapacities(capacityIds))
                .thenReturn(Flux.fromIterable(orphanedTechIds));

        // 2. El borrado de tecnologías falla (retorna false)
        when(technologyClientPort.deleteTechnolgies(orphanedTechIds))
                .thenReturn(Mono.just(false));

        // Act
        Mono<Boolean> result = capacityUseCase.deleteCapacities(capacityIds);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        // Verificaciones: No se debe intentar borrar las capacidades si falló lo anterior
        verify(capacityPersistencePort, never()).deleteCapacities(anyList());
    }
}