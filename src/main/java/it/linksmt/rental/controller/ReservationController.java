package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateReservationRequest;
import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.dto.ReservationStatisticsResponse;
import it.linksmt.rental.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping(value = "/create",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest reservationRequest) {
            ReservationResponse createdReservation = reservationService.createReservation(reservationRequest);
            return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(createdReservation);
    }
    @PostMapping(value = "/check-availability",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkReservationAvailability(@RequestBody CreateReservationRequest reservationRequest) {
        boolean isAvailable = reservationService.checkAvailability(reservationRequest);
        return ResponseEntity.ok(isAvailable);
    }
    @GetMapping(value="/vehicle-reservations/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationResponse>>listOfActiveOrFutureReservations(@PathVariable("id") Long id) {
        List<ReservationResponse>vehiclesReservations= reservationService.listOfActiveOrFutureReservations(id);
        return ResponseEntity.ok(vehiclesReservations);
    }

    @DeleteMapping(value = "/cancel/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {

        ReservationResponse cancelledReservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(cancelledReservation);
    }
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> reservations = reservationService.findAllReservations();
        return ResponseEntity.status(HttpStatus.OK).body(reservations);
    }

    @GetMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable("id") Long id) {
       ReservationResponse reservations=reservationService.getReservationById(id);
        return ResponseEntity.status(HttpStatus.OK).body(reservations);
    }
    @GetMapping(value = "/statistics/{MM-yyyy}",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationStatisticsResponse>> getReservationStatistics(@PathVariable("MM-yyyy") String date) {
        List<ReservationStatisticsResponse> statisticsResponses = reservationService.getReservationStatistics(date);
        return ResponseEntity.ok(statisticsResponses);
    }
  @GetMapping(value = "/reservation-list",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ReservationResponse>> getReservationListOfUser() {
        List<ReservationResponse> reservationResponseList=reservationService.getReservationListOfUser();
        return ResponseEntity.status(HttpStatus.OK).body(reservationResponseList);
  }

}
