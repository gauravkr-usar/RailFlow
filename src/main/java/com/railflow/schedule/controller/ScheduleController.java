package com.railflow.schedule.controller;

import com.railflow.schedule.dto.CreateScheduleRequest;
import com.railflow.schedule.dto.TrainScheduleDto;
import com.railflow.schedule.dto.UpdateScheduleRequest;
import com.railflow.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/trains/{trainId}/schedules")
    public ResponseEntity<List<TrainScheduleDto>> getSchedulesForTrain(
            @PathVariable Long trainId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(scheduleService.getSchedulesForTrain(trainId, fromDate, toDate));
    }

    @PostMapping("/trains/{trainId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainScheduleDto> createSchedule(
            @PathVariable Long trainId,
            @Valid @RequestBody CreateScheduleRequest request) {
        request.setTrainId(trainId);
        TrainScheduleDto created = scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrainScheduleDto> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    @PostMapping("/trains/{trainId}/schedules/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TrainScheduleDto>> generateSchedules(
            @PathVariable Long trainId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<TrainScheduleDto> schedules = scheduleService.generateSchedulesForDateRange(trainId, fromDate, toDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedules);
    }
}
