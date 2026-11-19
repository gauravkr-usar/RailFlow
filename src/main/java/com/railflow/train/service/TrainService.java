package com.railflow.train.service;

import com.railflow.common.enums.BerthType;
import com.railflow.station.entity.Station;
import com.railflow.station.repository.StationRepository;
import com.railflow.train.dto.*;
import com.railflow.train.entity.Coach;
import com.railflow.train.entity.Seat;
import com.railflow.train.entity.Train;
import com.railflow.train.repository.CoachRepository;
import com.railflow.train.repository.SeatRepository;
import com.railflow.train.repository.TrainRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrainService {

    private final TrainRepository trainRepository;
    private final StationRepository stationRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;

    public TrainDto createTrain(CreateTrainRequest request) {
        if (trainRepository.existsByTrainNumber(request.getTrainNumber())) {
            throw new IllegalArgumentException("Train with number " + request.getTrainNumber() + " already exists");
        }

        Station sourceStation = stationRepository.findById(request.getSourceStationId())
                .orElseThrow(() -> new EntityNotFoundException("Source station not found with id: " + request.getSourceStationId()));
        Station destStation = stationRepository.findById(request.getDestinationStationId())
                .orElseThrow(() -> new EntityNotFoundException("Destination station not found with id: " + request.getDestinationStationId()));

        Train train = Train.builder()
                .trainNumber(request.getTrainNumber())
                .trainName(request.getTrainName())
                .trainType(request.getTrainType())
                .sourceStation(sourceStation)
                .destinationStation(destStation)
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .runningDays(request.getRunningDays())
                .totalDistance(request.getTotalDistance())
                .isActive(true)
                .coaches(new ArrayList<>())
                .build();

        Train saved = trainRepository.save(train);
        log.info("Created train: {} - {}", saved.getTrainNumber(), saved.getTrainName());
        return mapToDto(saved, false);
    }

    public TrainDto updateTrain(Long id, CreateTrainRequest request) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + id));

        if (!train.getTrainNumber().equals(request.getTrainNumber()) &&
                trainRepository.existsByTrainNumber(request.getTrainNumber())) {
            throw new IllegalArgumentException("Train with number " + request.getTrainNumber() + " already exists");
        }

        Station sourceStation = stationRepository.findById(request.getSourceStationId())
                .orElseThrow(() -> new EntityNotFoundException("Source station not found with id: " + request.getSourceStationId()));
        Station destStation = stationRepository.findById(request.getDestinationStationId())
                .orElseThrow(() -> new EntityNotFoundException("Destination station not found with id: " + request.getDestinationStationId()));

        train.setTrainNumber(request.getTrainNumber());
        train.setTrainName(request.getTrainName());
        train.setTrainType(request.getTrainType());
        train.setSourceStation(sourceStation);
        train.setDestinationStation(destStation);
        train.setDepartureTime(request.getDepartureTime());
        train.setArrivalTime(request.getArrivalTime());
        train.setRunningDays(request.getRunningDays());
        train.setTotalDistance(request.getTotalDistance());

        Train saved = trainRepository.save(train);
        log.info("Updated train: {} - {}", saved.getTrainNumber(), saved.getTrainName());
        return mapToDto(saved, false);
    }

    public void deleteTrain(Long id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + id));
        train.setActive(false);
        trainRepository.save(train);
        log.info("Soft-deleted train id: {}", id);
    }

    @Transactional(readOnly = true)
    public TrainDto getTrain(Long id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + id));
        return mapToDto(train, false);
    }

    @Transactional(readOnly = true)
    public TrainDto getTrainByNumber(String trainNumber) {
        Train train = trainRepository.findByTrainNumber(trainNumber)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with number: " + trainNumber));
        return mapToDto(train, false);
    }

    @Transactional(readOnly = true)
    public TrainDto getTrainWithCoaches(Long id) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + id));
        return mapToDto(train, true);
    }

    @Transactional(readOnly = true)
    public Page<TrainDto> getAllTrains(Pageable pageable) {
        return trainRepository.findAll(pageable).map(t -> mapToDto(t, false));
    }

    @Transactional(readOnly = true)
    public Page<TrainDto> searchTrains(String query, Pageable pageable) {
        return trainRepository.searchByTrainNumberOrName(query, pageable).map(t -> mapToDto(t, false));
    }

    @Transactional(readOnly = true)
    public List<TrainDto> getTrainsBetweenStations(Long sourceStationId, Long destStationId) {
        Station source = stationRepository.findById(sourceStationId)
                .orElseThrow(() -> new EntityNotFoundException("Source station not found: " + sourceStationId));
        Station dest = stationRepository.findById(destStationId)
                .orElseThrow(() -> new EntityNotFoundException("Destination station not found: " + destStationId));
        return trainRepository.findBySourceStationAndDestinationStation(source, dest)
                .stream()
                .map(t -> mapToDto(t, false))
                .collect(Collectors.toList());
    }

    public CoachDto addCoach(Long trainId, AddCoachRequest request) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + trainId));

        if (coachRepository.existsByTrainAndCoachNumber(train, request.getCoachNumber())) {
            throw new IllegalArgumentException("Coach " + request.getCoachNumber() + " already exists on train " + train.getTrainNumber());
        }

        Coach coach = Coach.builder()
                .coachNumber(request.getCoachNumber().toUpperCase())
                .coachType(request.getCoachType())
                .train(train)
                .totalSeats(request.getTotalSeats())
                .isActive(true)
                .seats(new ArrayList<>())
                .build();

        Coach savedCoach = coachRepository.save(coach);

        // Auto-generate standard seats for the coach based on totalSeats and coachType
        generateDefaultSeatsForCoach(savedCoach);

        log.info("Added coach {} to train {}", savedCoach.getCoachNumber(), train.getTrainNumber());
        return mapCoachToDto(savedCoach, true);
    }

    public List<SeatDto> addSeatsToCoach(Long coachId, List<AddSeatRequest> seatRequests) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new EntityNotFoundException("Coach not found with id: " + coachId));

        List<Seat> seatsToSave = new ArrayList<>();
        for (AddSeatRequest sr : seatRequests) {
            if (seatRepository.existsByCoachAndSeatNumber(coach, sr.getSeatNumber())) {
                continue;
            }
            Seat seat = Seat.builder()
                    .coach(coach)
                    .seatNumber(sr.getSeatNumber())
                    .berthType(sr.getBerthType())
                    .isActive(true)
                    .build();
            seatsToSave.add(seat);
        }

        List<Seat> saved = seatRepository.saveAll(seatsToSave);
        log.info("Added {} seats to coach {}", saved.size(), coach.getCoachNumber());
        return saved.stream().map(this::mapSeatToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CoachDto> getCoachesForTrain(Long trainId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new EntityNotFoundException("Train not found with id: " + trainId));
        return coachRepository.findByTrain(train)
                .stream()
                .map(c -> mapCoachToDto(c, true))
                .collect(Collectors.toList());
    }

    private void generateDefaultSeatsForCoach(Coach coach) {
        List<Seat> seats = new ArrayList<>();
        int count = coach.getTotalSeats() != null ? coach.getTotalSeats() : 72;
        for (int i = 1; i <= count; i++) {
            BerthType berthType = determineBerthType(coach.getCoachType(), i);
            seats.add(Seat.builder()
                    .coach(coach)
                    .seatNumber(i)
                    .berthType(berthType)
                    .isActive(true)
                    .build());
        }
        seatRepository.saveAll(seats);
        coach.setSeats(seats);
    }

    private BerthType determineBerthType(com.railflow.common.enums.CoachType type, int seatNum) {
        if (type == com.railflow.common.enums.CoachType.CC || type == com.railflow.common.enums.CoachType.EC) {
            int mod = seatNum % 5;
            return (mod == 1 || mod == 0) ? BerthType.WINDOW : (mod == 2 || mod == 4 ? BerthType.AISLE : BerthType.MIDDLE);
        }
        int mod = (seatNum - 1) % 8;
        return switch (mod) {
            case 0, 3 -> BerthType.LOWER;
            case 1, 4 -> BerthType.MIDDLE;
            case 2, 5 -> BerthType.UPPER;
            case 6 -> BerthType.SIDE_LOWER;
            case 7 -> BerthType.SIDE_UPPER;
            default -> BerthType.LOWER;
        };
    }

    public TrainDto mapToDto(Train train, boolean includeCoaches) {
        TrainDto.TrainDtoBuilder builder = TrainDto.builder()
                .id(train.getId())
                .trainNumber(train.getTrainNumber())
                .trainName(train.getTrainName())
                .trainType(train.getTrainType())
                .departureTime(train.getDepartureTime())
                .arrivalTime(train.getArrivalTime())
                .runningDays(train.getRunningDays())
                .totalDistance(train.getTotalDistance())
                .isActive(train.isActive())
                .createdAt(train.getCreatedAt())
                .updatedAt(train.getUpdatedAt());

        if (train.getSourceStation() != null) {
            builder.sourceStationId(train.getSourceStation().getId())
                   .sourceStationCode(train.getSourceStation().getStationCode())
                   .sourceStationName(train.getSourceStation().getStationName());
        }
        if (train.getDestinationStation() != null) {
            builder.destinationStationId(train.getDestinationStation().getId())
                   .destinationStationCode(train.getDestinationStation().getStationCode())
                   .destinationStationName(train.getDestinationStation().getStationName());
        }

        if (includeCoaches && train.getCoaches() != null) {
            builder.coaches(train.getCoaches().stream()
                    .map(c -> mapCoachToDto(c, true))
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public CoachDto mapCoachToDto(Coach coach, boolean includeSeats) {
        CoachDto.CoachDtoBuilder builder = CoachDto.builder()
                .id(coach.getId())
                .coachNumber(coach.getCoachNumber())
                .coachType(coach.getCoachType())
                .trainId(coach.getTrain() != null ? coach.getTrain().getId() : null)
                .totalSeats(coach.getTotalSeats())
                .isActive(coach.isActive());

        if (includeSeats && coach.getSeats() != null) {
            builder.seats(coach.getSeats().stream()
                    .map(this::mapSeatToDto)
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    public SeatDto mapSeatToDto(Seat seat) {
        return SeatDto.builder()
                .id(seat.getId())
                .seatNumber(seat.getSeatNumber())
                .berthType(seat.getBerthType())
                .coachId(seat.getCoach() != null ? seat.getCoach().getId() : null)
                .isActive(seat.isActive())
                .build();
    }
}
