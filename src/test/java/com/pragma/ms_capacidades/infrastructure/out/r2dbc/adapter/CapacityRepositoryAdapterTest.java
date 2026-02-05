package com.pragma.ms_capacidades.infrastructure.out.r2dbc.adapter;

import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.infrastructure.exception.InvalidSortFieldException;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityEntity;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityTechnologyEntity;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.mapper.ICapacityEntityMapper;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.projections.CapacityWithTechCountProjection;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository.ICapacityR2dbcRepository;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.repository.ICapacityTechnologyR2dbcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityRepositoryAdapterTest {

    @Mock
    private ICapacityTechnologyR2dbcRepository capacityTechnologyRepository;
    @Mock
    private ICapacityR2dbcRepository capacityR2dbcRepository;
    @Mock
    private ICapacityEntityMapper capacityEntityMapper;
    @Mock
    private DatabaseClient databaseClient;

    // Mocks necesarios para la cadena fluida de DatabaseClient
    @Mock
    private DatabaseClient.GenericExecuteSpec genericExecuteSpec;
    @Mock
    private RowsFetchSpec<CapacityWithTechCountProjection> rowsFetchSpec;

    @InjectMocks
    private CapacityRepositoryAdapter capacityRepositoryAdapter;

    // --- TEST: save ---

    @Test
    @DisplayName("Save: Should save capacity entity and technology relations")
    void save_ShouldPersistAndReturnModel() {
        // Arrange
        List<Long> techIds = Arrays.asList(10L, 20L);
        Capacity capacityModel = new Capacity(null, "Java Cap", "Desc", techIds, null, null);

        // Entidades simuladas
        CapacityEntity capacityEntity = new CapacityEntity(); // Asumiendo setters o constructor
        capacityEntity.setName("Java Cap");

        CapacityEntity savedEntity = new CapacityEntity();
        savedEntity.setId(1L);
        savedEntity.setTechnologyIds(techIds); // Importante para el flujo interno

        Capacity savedModel = new Capacity(1L, "Java Cap", "Desc", techIds, null, null);

        // 1. Mapeo inicial
        when(capacityEntityMapper.toEntity(capacityModel)).thenReturn(capacityEntity);

        // 2. Guardado de la entidad padre
        when(capacityR2dbcRepository.save(capacityEntity)).thenReturn(Mono.just(savedEntity));

        // 3. Guardado de las relaciones (se llamará 2 veces porque hay 2 techIds)
        when(capacityTechnologyRepository.save(any(CapacityTechnologyEntity.class)))
                .thenReturn(Mono.just(new CapacityTechnologyEntity())); // Retorno dummy

        // 4. Mapeo final
        when(capacityEntityMapper.toModel(savedEntity)).thenReturn(savedModel);

        // Act
        Mono<Capacity> result = capacityRepositoryAdapter.save(capacityModel);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedModel)
                .verifyComplete();

        verify(capacityR2dbcRepository).save(capacityEntity);
        // Verificamos que se guardaron las relaciones en la tabla intermedia
        verify(capacityTechnologyRepository, times(2)).save(any(CapacityTechnologyEntity.class));
    }

    // --- TEST: findAllPaged ---

    @Test
    @DisplayName("FindAllPaged: Should execute correct SQL and map results")
    void findAllPaged_ShouldReturnPagedCapacities() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "asc";

        CapacityWithTechCountProjection projection = new CapacityWithTechCountProjection(1L, "Name", "Desc", 5L);
        Capacity capacityModel = new Capacity(1L, "Name", "Desc", null, null, 5);

        // Mocking DatabaseClient fluent API
        when(databaseClient.sql(anyString())).thenReturn(genericExecuteSpec);
        // Usamos any(BiFunction.class) para evitar ambigüedad
        when(genericExecuteSpec.map(any(BiFunction.class))).thenReturn(rowsFetchSpec);
        when(rowsFetchSpec.all()).thenReturn(Flux.just(projection));

        when(capacityEntityMapper.toModelProjection(projection)).thenReturn(capacityModel);

        // Act
        Flux<Capacity> result = capacityRepositoryAdapter.findAllPaged(page, size, sortBy, direction);

        // Assert
        StepVerifier.create(result)
                .expectNext(capacityModel)
                .verifyComplete();

        // Verificación del SQL generado
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        String executedSql = sqlCaptor.getValue();

        assertTrue(executedSql.contains("ORDER BY c.name ASC"));
        assertTrue(executedSql.contains("LIMIT 10 OFFSET 0"));
    }

    @Test
    @DisplayName("FindAllPaged: Should throw exception for invalid sort field")
    void findAllPaged_WhenInvalidSort_ShouldThrowException() {
        // Act & Assert
        assertThrows(InvalidSortFieldException.class, () -> {
            capacityRepositoryAdapter.findAllPaged(0, 10, "invalid_field", "asc");
        });
    }

    // --- TEST: existsByName ---

    @Test
    @DisplayName("ExistsByName: Should delegate to repository")
    void existsByName_ShouldReturnRepoResult() {
        // Arrange
        String name = "Test";
        when(capacityR2dbcRepository.existsByName(name)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = capacityRepositoryAdapter.existsByName(name);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    // --- TEST: count ---

    @Test
    @DisplayName("Count: Should delegate to repository")
    void count_ShouldReturnRepoResult() {
        // Arrange
        when(capacityR2dbcRepository.countAll()).thenReturn(Mono.just(10L));

        // Act
        Mono<Long> result = capacityRepositoryAdapter.count();

        // Assert
        StepVerifier.create(result)
                .expectNext(10L)
                .verifyComplete();
    }

    // --- TEST: findTechnologyIdsByCapacityId ---

    @Test
    @DisplayName("FindTechnologyIds: Should delegate to tech repository")
    void findTechnologyIdsByCapacityId_ShouldReturnFlux() {
        // Arrange
        Long capId = 1L;
        when(capacityTechnologyRepository.findTechnologyIdsByCapacityId(capId))
                .thenReturn(Flux.just(10L, 20L));

        // Act
        Flux<Long> result = capacityRepositoryAdapter.findTechnologyIdsByCapacityId(capId);

        // Assert
        StepVerifier.create(result)
                .expectNext(10L)
                .expectNext(20L)
                .verifyComplete();
    }

    // --- TEST: findCapacitiesByIds ---

    @Test
    @DisplayName("FindCapacitiesByIds: Should find entities and map to models")
    void findCapacitiesByIds_ShouldReturnModels() {
        // Arrange
        List<Long> ids = Arrays.asList(1L);
        CapacityEntity entity = new CapacityEntity();
        entity.setId(1L);
        Capacity model = new Capacity(1L, "Name", "Desc", null, null, null);

        when(capacityR2dbcRepository.findByIdIn(ids)).thenReturn(Flux.just(entity));
        when(capacityEntityMapper.toModel(entity)).thenReturn(model);

        // Act
        Flux<Capacity> result = capacityRepositoryAdapter.findCapacitiesByIds(ids);

        // Assert
        StepVerifier.create(result)
                .expectNext(model)
                .verifyComplete();
    }

    // --- TEST: deleteCapacities ---

    @Test
    @DisplayName("DeleteCapacities: Should delete relations first, then capacities")
    void deleteCapacities_ShouldReturnTrue() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);

        // 1. Borrado de tabla intermedia
        when(capacityTechnologyRepository.deleteAllByCapacityIdIn(ids)).thenReturn(Mono.empty());
        // 2. Borrado de tabla principal
        when(capacityR2dbcRepository.deleteAllById(ids)).thenReturn(Mono.empty());

        // Act
        Mono<Boolean> result = capacityRepositoryAdapter.deleteCapacities(ids);

        // Assert
        StepVerifier.create(result)
                .expectNext(true) // .thenReturn(true)
                .verifyComplete();

        verify(capacityTechnologyRepository).deleteAllByCapacityIdIn(ids);
        verify(capacityR2dbcRepository).deleteAllById(ids);
    }

    @Test
    @DisplayName("DeleteCapacities: Should propagate error if deletion fails")
    void deleteCapacities_OnError_ShouldReturnError() {
        // Arrange
        List<Long> ids = Arrays.asList(1L);

        // Simulamos que el primer paso falla
        when(capacityTechnologyRepository.deleteAllByCapacityIdIn(ids))
                .thenReturn(Mono.error(new RuntimeException("DB Error")));

        when(capacityR2dbcRepository.deleteAllById(ids))
                .thenReturn(Mono.empty());

        // Act
        Mono<Boolean> result = capacityRepositoryAdapter.deleteCapacities(ids);

        // Assert
        StepVerifier.create(result)
                .expectErrorMessage("DB Error")
                .verify();

    }

    // --- TEST: findTechnologiesNotReferencedInOtherCapacities ---

    @Test
    @DisplayName("FindTechnologiesNotReferenced: Should delegate to repository")
    void findTechnologiesNotReferenced_ShouldReturnFlux() {
        // Arrange
        List<Long> capIds = Arrays.asList(1L);
        when(capacityTechnologyRepository.findTechnologiesNotReferencedInOtherCapacities(capIds))
                .thenReturn(Flux.just(100L));

        // Act
        Flux<Long> result = capacityRepositoryAdapter.findTechnologiesNotReferencedInOtherCapacities(capIds);

        // Assert
        StepVerifier.create(result)
                .expectNext(100L)
                .verifyComplete();
    }
}