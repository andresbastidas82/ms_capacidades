package com.pragma.ms_capacidades.infrastructure.out.r2dbc.mapper;

import com.pragma.ms_capacidades.domain.model.Capacity;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.entity.CapacityEntity;
import com.pragma.ms_capacidades.infrastructure.out.r2dbc.projections.CapacityWithTechCountProjection;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ICapacityEntityMapper {

    CapacityEntity toEntity(Capacity capacity);

    Capacity toModel(CapacityEntity capacityEntity);

    Capacity toModelProjection(CapacityWithTechCountProjection projection);
}
