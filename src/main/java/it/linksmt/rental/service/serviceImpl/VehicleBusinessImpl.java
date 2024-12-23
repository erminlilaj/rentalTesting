package it.linksmt.rental.service.serviceImpl;

import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.service.ReservationService;
import it.linksmt.rental.service.VehicleBusinessLayer;
import it.linksmt.rental.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
public class VehicleBusinessImpl implements VehicleBusinessLayer {
    private final ReservationService reservationService;
private final VehicleService vehicleService;
    public VehicleBusinessImpl(ReservationService reservationService, VehicleService vehicleService) {
        this.reservationService = reservationService;
        this.vehicleService = vehicleService;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<ReservationResponse> deleteVehicle(Long id) {
        vehicleService.deleteVehicle(id);
        reservationService.cancelReservationsOfVehicle(id);

        return reservationService.cancelReservationsOfVehicle(id);
    }

    @Override
    public List<ReservationResponse> activateOrFutureReservationOfVehicle(Long id) {
        return reservationService.listOfActiveOrFutureReservations(id);
    }
}
