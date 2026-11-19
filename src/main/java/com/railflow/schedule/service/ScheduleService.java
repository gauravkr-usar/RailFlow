package com.railflow.schedule.service;

import com.railflow.common.enums.DayOfWeekFlag;
import com.railflow.common.enums.ScheduleStatus;
import com.railflow.schedule.dto.CreateScheduleRequest;
import com.railflow.schedule.dto.TrainScheduleDto;
import com.railflow.schedule.dto.UpdateScheduleRequest;
import com.railflow.schedule.entity.TrainRun;
import com.railflow.schedule.entity.TrainSchedule;
import com.railflow.schedule.repository.TrainRunRepository;
import com.railflow.schedule.repository.TrainScheduleRepository;
import com.railflow.train.entity.Train;
import com.railflow.train.repository.TrainRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScheduleService {

    private final TrainScheduleRepository trainScheduleRepository;
    private final TrainRunRepository trainRunRepository;
    private final TrainRepository trainRepository;

    public TrainScheduleDto createSchedule(CreateScheduleRequest request) {
        Train train = trainRepository.findById(request.getTrainId())
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + request.getTrainId()));

        if (trainScheduleRepository.existsByTrainAndJourneyDate(train, request.getJourneyDate())) {
            throw new IllegalArgumentException("Schedule already exists for train " + train.getTrainNumber() + " on " + request.getJourneyDate());
        }

        TrainSchedule schedule = TrainSchedule.builder()
                .train(train)
                .journeyDate(request.getJourneyDate())
                .status(request.getStatus() != null ? request.getStatus() : ScheduleStatus.ON_TIME)
                .delayMinutes(request.getDelayMinutes() != null ? request.getDelayMinutes() : 0)
                .remarks(request.getRemarks())
                .build();

        TrainSchedule saved = trainScheduleRepository.save(schedule);

        // Also ensure TrainRun exists
        if (!trainRunRepository.existsByTrainAndRunDate(train, request.getJourneyDate())) {
            TrainRun run = TrainRun.builder()
                    .train(train)
                    .runDate(request.getJourneyDate())
                    .isActive(true)
                    .scheduledDepartureTime(train.getDepartureTime())
                    .actualDepartureTime(train.getDepartureTime())
                    .build();
            trainRunRepository.save(run);
        }

        log.info("Created schedule for train {} on {}", train.getTrainNumber(), request.getJourneyDate());
        return mapToDto(saved);
    }

    public TrainScheduleDto updateSchedule(Long scheduleId, UpdateScheduleRequest request) {
        TrainSchedule schedule = trainScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with id: " + scheduleId));

        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }
        if (request.getDelayMinutes() != null) {
            schedule.setDelayMinutes(request.getDelayMinutes());
        }
        if (request.getRemarks() != null) {
            schedule.setRemarks(request.getRemarks());
        }

        TrainSchedule saved = trainScheduleRepository.save(schedule);
        log.info("Updated schedule id: {}", scheduleId);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TrainScheduleDto> getSchedulesForTrain(Long trainId, LocalDate fromDate, LocalDate toDate) {
        return trainScheduleRepository.findByTrainIdAndJourneyDateBetweenOrderByJourneyDateAsc(trainId, fromDate, toDate)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TrainScheduleDto> generateSchedulesForDateRange(Long trainId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }

        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + trainId));

        List<TrainSchedule> schedulesToSave = new ArrayList<>();
        List<TrainRun> runsToSave = new ArrayList<>();

        LocalDate current = fromDate;
        while (!current.isAfter(toDate)) {
            DayOfWeek dow = current.getDayOfWeek();
            DayOfWeekFlag flag = DayOfWeekFlag.valueOf(dow.name());

            if (train.getRunningDays().contains(flag)) {
                if (!trainScheduleRepository.existsByTrainAndJourneyDate(train, current)) {
                    TrainSchedule schedule = TrainSchedule.builder()
                            .train(train)
                            .journeyDate(current)
                            .status(ScheduleStatus.ON_TIME)
                            .delayMinutes(0)
                            .build();
                    schedulesToSave.add(schedule);
                }

                if (!trainRunRepository.existsByTrainAndRunDate(train, current)) {
                    TrainRun run = TrainRun.builder()
                            .train(train)
                            .runDate(current)
                            .isActive(true)
                            .scheduledDepartureTime(train.getDepartureTime())
                            .actualDepartureTime(train.getDepartureTime())
                            .build();
                    runsToSave.add(run);
                }
            }
            current = current.plusDays(1);
        }

        if (!runsToSave.isEmpty()) {
            trainRunRepository.saveAll(runsToSave);
        }

        List<TrainSchedule> savedSchedules = schedulesToSave.isEmpty()
                ? List.of()
                : trainScheduleRepository.saveAll(schedulesToSave);

        log.info("Generated {} schedules and {} runs for train {} between {} and {}",
                savedSchedules.size(), runsToSave.size(), train.getTrainNumber(), fromDate, toDate);

        return getSchedulesForTrain(trainId, fromDate, toDate);
    }

    public TrainScheduleDto mapToDto(TrainSchedule schedule) {
        Train train = schedule.getTrain();
        return TrainScheduleDto.builder()
                .id(schedule.getId())
                .trainId(train != null ? train.getId() : null)
                .trainNumber(train != null ? train.getTrainNumber() : null)
                .trainName(train != null ? train.getTrainName() : null)
                .journeyDate(schedule.getJourneyDate())
                .status(schedule.getStatus())
                .delayMinutes(schedule.getDelayMinutes())
                .remarks(schedule.getRemarks())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
