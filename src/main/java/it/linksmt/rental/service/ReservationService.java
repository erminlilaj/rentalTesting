package it.linksmt.rental.service;

import it.linksmt.rental.dto.CreateReservationRequest;
import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.dto.ReservationStatisticsResponse;

import java.util.List;

public interface ReservationService {


    ReservationResponse createReservation(CreateReservationRequest reservationRequest);
    ReservationResponse getReservationById(Long id);
    List<ReservationResponse> findAllReservations();
    ReservationResponse cancelReservation(Long id);
    boolean checkAvailability(CreateReservationRequest reservationRequest);

    List<ReservationStatisticsResponse> getReservationStatistics(String date);

    List<ReservationResponse> getReservationListOfUser();

   List<ReservationResponse> listOfActiveOrFutureReservations(Long id);
   List<ReservationResponse> cancelReservationsOfVehicle(Long vehicleId);
}
