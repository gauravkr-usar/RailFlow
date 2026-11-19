package com.railflow.train.controller;

import com.railflow.common.enums.CoachType;
import com.railflow.inventory.dto.AvailabilityResponse;
import com.railflow.train.dto.TrainDto;
import com.railflow.train.dto.TrainSearchRequest;
import com.railflow.train.dto.TrainSearchResult;
import com.railflow.train.service.TrainSearchService;
import com.railflow.train.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trains")
@RequiredArgsConstructor
@Slf4j
public class TrainSearchController {

    private final TrainSearchService trainSearchService;
    private final TrainService trainService;

    @GetMapping("/search")
    public ResponseEntity<?> searchTrains(
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "class", required = false) CoachType coachType,
            @RequestParam(value = "passengers", defaultValue = "1") Integer passengers,
            @RequestParam(value = "q", required = false) String query,
            @PageableDefault(size = 10) Pageable pageable) {

        if (from != null && to != null && date != null) {
            TrainSearchRequest request = TrainSearchRequest.builder()
                    .sourceStationCode(from)
                    .destStationCode(to)
                    .journeyDate(date)
                    .coachType(coachType)
                    .passengerCount(passengers)
                    .build();
            List<TrainSearchResult> results = trainSearchService.searchTrains(request);
            return ResponseEntity.ok(results);
        } else if (query != null && !query.isBlank()) {
            Page<TrainDto> page = trainService.searchTrains(query, pageable);
            return ResponseEntity.ok(page);
        } else {
            return ResponseEntity.badRequest().body("Either 'from', 'to', and 'date' or 'q' parameter must be provided");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainDto> getTrainById(@PathVariable Long id) {
        TrainDto trainDto = trainSearchService.getTrainWithFullRoute(id);
        return ResponseEntity.ok(trainDto);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> getTrainAvailability(
            @PathVariable Long id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam("class") CoachType coachType) {
        AvailabilityResponse response = trainSearchService.getTrainAvailability(id, date, coachType);
        return ResponseEntity.ok(response);
    }
}
