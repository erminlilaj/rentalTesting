package it.linksmt.rental.service;

import it.linksmt.rental.dto.ReservationResponse;

import java.util.List;

public interface VehicleBusinessLayer {
    List<ReservationResponse> deleteVehicle(Long id);
    List<ReservationResponse> activateOrFutureReservationOfVehicle(Long id);
}
