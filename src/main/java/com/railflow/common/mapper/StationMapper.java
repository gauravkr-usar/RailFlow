package com.railflow.common.mapper;

import com.railflow.station.dto.StationDto;
import com.railflow.station.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StationMapper {

    StationDto toDto(Station station);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Station toEntity(StationDto dto);
}
