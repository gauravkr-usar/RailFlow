package com.railflow.waitinglist.service;

import com.railflow.booking.entity.BookingPassenger;
import com.railflow.booking.repository.BookingPassengerRepository;
import com.railflow.common.enums.CoachType;
import com.railflow.common.enums.PassengerStatus;
import com.railflow.common.enums.WaitingListStatus;
import com.railflow.waitinglist.entity.WaitingListEntry;
import com.railflow.waitinglist.repository.WaitingListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitingListService {

    private final WaitingListRepository waitingListRepository;
    private final BookingPassengerRepository bookingPassengerRepository;

    @Transactional
    public WaitingListEntry addToWaitingList(Long bookingId, Long passengerId, Long trainId, LocalDate date, CoachType coachType) {
        int nextPos = waitingListRepository.findMaxPosition(trainId, date, coachType, WaitingListStatus.WAITING_LIST)
                .orElse(0) + 1;

        WaitingListEntry entry = WaitingListEntry.builder()
                .bookingId(bookingId)
                .passengerId(passengerId)
                .trainId(trainId)
                .journeyDate(date)
                .coachType(coachType)
                .position(nextPos)
                .status(WaitingListStatus.WAITING_LIST)
                .build();

        log.info("Passenger {} added to Waiting List position {} for train {} on {}", passengerId, nextPos, trainId, date);
        return waitingListRepository.save(entry);
    }

    @Transactional
    public List<WaitingListEntry> promoteEligiblePassengers(Long trainId, LocalDate date, CoachType coachType, int releasedSeatsCount) {
        log.info("Processing promotion for train {} on {}, released seats: {}", trainId, date, releasedSeatsCount);
        List<WaitingListEntry> promotedEntries = new ArrayList<>();

        if (releasedSeatsCount <= 0) {
            return promotedEntries;
        }

        // 1. Promote RAC to CONFIRMED
        List<WaitingListEntry> racList = waitingListRepository.findByTrainIdAndJourneyDateAndCoachTypeAndStatusOrderByPositionAsc(
                trainId, date, coachType, WaitingListStatus.RAC
        );

        int remainingSeats = releasedSeatsCount;
        for (WaitingListEntry rac : racList) {
            if (remainingSeats <= 0) break;

            rac.setStatus(WaitingListStatus.CONFIRMED);
            waitingListRepository.save(rac);
            promotedEntries.add(rac);

            // Update BookingPassenger record
            updatePassengerStatus(rac.getPassengerId(), PassengerStatus.CONFIRMED);
            remainingSeats--;
            log.info("RAC passenger {} promoted to CONFIRMED", rac.getPassengerId());
        }

        // 2. Promote WL to RAC or CONFIRMED
        List<WaitingListEntry> wlList = waitingListRepository.findByTrainIdAndJourneyDateAndCoachTypeAndStatusOrderByPositionAsc(
                trainId, date, coachType, WaitingListStatus.WAITING_LIST
        );

        for (WaitingListEntry wl : wlList) {
            if (remainingSeats > 0) {
                wl.setStatus(WaitingListStatus.CONFIRMED);
                updatePassengerStatus(wl.getPassengerId(), PassengerStatus.CONFIRMED);
                remainingSeats--;
            } else {
                wl.setStatus(WaitingListStatus.RAC);
                updatePassengerStatus(wl.getPassengerId(), PassengerStatus.RAC);
            }
            waitingListRepository.save(wl);
            promotedEntries.add(wl);
            log.info("WL passenger {} promoted to {}", wl.getPassengerId(), wl.getStatus());
        }

        return promotedEntries;
    }

    private void updatePassengerStatus(Long passengerId, PassengerStatus newStatus) {
        try {
            bookingPassengerRepository.findById(passengerId).ifPresent(bp -> {
                bp.setStatus(newStatus);
                bookingPassengerRepository.save(bp);
            });
        } catch (Exception e) {
            log.warn("Could not update passenger status for ID {}: {}", passengerId, e.getMessage());
        }
    }
}
