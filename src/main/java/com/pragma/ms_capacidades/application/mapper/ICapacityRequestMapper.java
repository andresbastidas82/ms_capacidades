package com.pragma.ms_capacidades.application.mapper;

import com.pragma.ms_capacidades.application.dto.CapacityRequest;
import com.pragma.ms_capacidades.application.dto.CapacityResponse;
import com.pragma.ms_capacidades.domain.model.Capacity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface ICapacityRequestMapper {

    Capacity toCapacityModel(CapacityRequest capacityRequest);

    CapacityResponse toCapacityResponse(Capacity capacity);
}
