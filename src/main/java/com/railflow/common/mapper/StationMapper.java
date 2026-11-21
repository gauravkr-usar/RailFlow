package com.railflow.common.mapper;

import com.railflow.station.dto.StationDto;
import com.railflow.station.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StationMapper {

    @Mapping(target = "isActive", source = "active")
    StationDto toDto(Station station);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", source = "active")
    Station toEntity(StationDto dto);
}
