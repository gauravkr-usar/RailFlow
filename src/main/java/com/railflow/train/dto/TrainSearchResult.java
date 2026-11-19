package com.railflow.train.dto;

import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.TrainType;
import com.railflow.station.dto.StationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainSearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long trainId;
    private String trainNumber;
    private String trainName;
    private TrainType trainType;

    private StationDto sourceStation;
    private StationDto destStation;

    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private String duration; // e.g. "14h 30m"

    private LocalDate journeyDate;

    @Builder.Default
    private Map<CoachType, AvailabilityInfo> availability = new HashMap<>();

    @Builder.Default
    private Map<CoachType, BigDecimal> fares = new HashMap<>();
}
