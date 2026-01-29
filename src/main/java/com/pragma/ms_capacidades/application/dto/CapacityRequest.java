package com.pragma.ms_capacidades.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CapacityRequest {
    private String name;
    private String description;
    private List<Long> technologyIds;
}
