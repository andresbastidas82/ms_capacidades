package com.pragma.ms_capacidades.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Capacity {
    private Long id;
    private String name;
    private String description;
    private List<Long> technologyIds;
}