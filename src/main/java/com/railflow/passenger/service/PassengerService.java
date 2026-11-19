package com.railflow.passenger.service;

import com.railflow.booking.dto.PassengerRequest;
import com.railflow.exception.ResourceNotFoundException;
import com.railflow.passenger.dto.PassengerResponse;
import com.railflow.passenger.entity.Passenger;
import com.railflow.passenger.repository.PassengerRepository;
import com.railflow.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassengerService {

    private final PassengerRepository passengerRepository;

    @Transactional
    public PassengerResponse addPassengerProfile(User user, PassengerRequest request) {
        log.info("Adding passenger profile for user: {}, name: {}", user.getEmail(), request.getName());
        Passenger passenger = Passenger.builder()
                .user(user)
                .name(request.getName().trim())
                .age(request.getAge())
                .gender(request.getGender())
                .idProofType(request.getIdProofType())
                .idProofNumber(request.getIdProofNumber())
                .isActive(true)
                .build();

        Passenger saved = passengerRepository.save(passenger);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PassengerResponse> getMyPassengers(User user) {
        log.info("Fetching saved passenger profiles for user: {}", user.getEmail());
        return passengerRepository.findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePassengerProfile(Long passengerId, User user) {
        log.info("Deleting passenger profile id: {} for user: {}", passengerId, user.getEmail());
        Passenger passenger = passengerRepository.findByIdAndUserId(passengerId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger", "id", passengerId));

        passenger.setActive(false);
        passengerRepository.save(passenger);
    }

    public PassengerResponse mapToResponse(Passenger passenger) {
        return PassengerResponse.builder()
                .id(passenger.getId())
                .userId(passenger.getUser() != null ? passenger.getUser().getId() : null)
                .name(passenger.getName())
                .age(passenger.getAge())
                .gender(passenger.getGender())
                .idProofType(passenger.getIdProofType())
                .idProofNumber(passenger.getIdProofNumber())
                .isActive(passenger.isActive())
                .createdAt(passenger.getCreatedAt())
                .updatedAt(passenger.getUpdatedAt())
                .build();
    }
}
