package com.railflow.route.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {

    @NotEmpty(message = "At least two stations are required in a route")
    @Valid
    private List<CreateRouteStationRequest> stations;
}
