package it.linksmt.rental.service.serviceImpl;

import it.linksmt.rental.dto.CreateReservationRequest;
import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.dto.ReservationStatisticsResponse;
import it.linksmt.rental.entity.ReservationEntity;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.ErrorCode;
import it.linksmt.rental.enums.ReservationStatus;
import it.linksmt.rental.enums.VehicleStatus;
import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.ReservationRepository;
import it.linksmt.rental.repository.UserRepository;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.repository.projections.ReservationStatisticsProjection;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.ReservationService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final VehicleServiceImpl vehicleServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private ReservationRepository reservationRepository;
    private AuthenticationService authenticationService;
    private VehicleRepository vehicleRepository;
    private UserRepository userRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, AuthenticationService authenticationService,
                                  VehicleRepository vehicleRepository, UserRepository userRepository, VehicleServiceImpl vehicleServiceImpl, UserServiceImpl userServiceImpl) {
        this.reservationRepository = reservationRepository;
        this.authenticationService = authenticationService;
        this.vehicleRepository=vehicleRepository;
        this.userRepository=userRepository;
        this.vehicleServiceImpl = vehicleServiceImpl;
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    public ReservationResponse createReservation(CreateReservationRequest reservationRequest) {
        Long currentUserId = authenticationService.getCurrentUserId();
        VehicleEntity requestedVehicle = vehicleServiceImpl.getVehicleById(reservationRequest.getVehicleId());

        if (!checkAvailability(reservationRequest)) {
            throw new ServiceException(ErrorCode.VEHICLE_NOT_AVAILABLE, "Vehicle is not available for the selected dates");
        }

        ReservationEntity savedReservation = saveReservationDetails(reservationRequest, requestedVehicle, currentUserId);
        return convertReservationToResponse(savedReservation);
    }
    @Override
    public boolean checkAvailability(CreateReservationRequest reservationRequest) {
        VehicleEntity requestedVehicle = vehicleServiceImpl.getVehicleById(reservationRequest.getVehicleId());

        // Detailed logging
        System.out.println("Requested Vehicle ID: " + requestedVehicle.getId());
        System.out.println("Vehicle Status: " + requestedVehicle.getVehicleStatus());

        boolean isVehicleBusy = reservationRepository.areDatesOverlapping(
                requestedVehicle.getId(),
                reservationRequest.getStartDate(),
                reservationRequest.getEndDate()
        );

        System.out.println("Is Vehicle Busy (Overlapping): " + isVehicleBusy);

        boolean isAvailable = !requestedVehicle.getVehicleStatus().equals(VehicleStatus.MAINTENANCE) && !isVehicleBusy;

        System.out.println("Detailed Availability Check:");
        System.out.println("Vehicle Status Check: " + !requestedVehicle.getVehicleStatus().equals(VehicleStatus.MAINTENANCE));
        System.out.println("Vehicle Busy Check: " + !isVehicleBusy);
        System.out.println("Final Availability: " + isAvailable);

        return isAvailable;
    }


    public ReservationResponse convertReservationToResponse(ReservationEntity savedReservation) {
        ReservationResponse reservationResponse=new ReservationResponse();
        reservationResponse.setReservationId(savedReservation.getId());
        reservationResponse.setUserId(savedReservation.getUser().getId());
        reservationResponse.setVehicleId(savedReservation.getVehicle().getId());
        reservationResponse.setVehicleName(savedReservation.getVehicle().getBrand()+" "+savedReservation.getVehicle().getModel());
        reservationResponse.setStartDate(savedReservation.getStartDate());
        reservationResponse.setEndDate(savedReservation.getEndDate());
        reservationResponse.setStatus(savedReservation.getStatus().toString());
        reservationResponse.setDuration(savedReservation.getDurationDays());
        reservationResponse.setPrice(savedReservation.getTotalPrice());

        return reservationResponse;
    }

    public ReservationEntity saveReservationDetails(CreateReservationRequest reservationRequest,VehicleEntity vehicleEntity,Long userId) {
        ReservationEntity reservationEntity=new ReservationEntity();

        int durationDays = (int) Duration.between(reservationRequest.getStartDate(), reservationRequest.getEndDate()).toDays();
        double totalPrice= durationDays* vehicleServiceImpl.getVehiclePrice(vehicleEntity.getId());


        reservationEntity.setUser(userServiceImpl.getUserById(userId));
        reservationEntity.setVehicle(vehicleEntity);
        reservationEntity.setStartDate(reservationRequest.getStartDate());
        reservationEntity.setEndDate(reservationRequest.getEndDate());
        reservationEntity.setStatus(ReservationStatus.RESERVED);
        reservationEntity.setTotalPrice(totalPrice);
        reservationEntity.setDurationDays(durationDays);

        return reservationRepository.save(reservationEntity);
    }

    @Override
    public ReservationResponse getReservationById(Long id) {

        ReservationEntity reservationEntity = reservationRepository.findById(id).orElse(null);
        if(reservationEntity==null) {
            throw new ServiceException(
                    ErrorCode.RESERVATION_NOT_FOUND,
                    "Reservation not found"
            );
        }
        return convertReservationToResponse(reservationEntity);
    }

    @Override
    public List<ReservationResponse> findAllReservations() {
        List<ReservationEntity> reservationsList=reservationRepository.findAll();
        if(reservationsList==null){
            throw new ServiceException(
                    ErrorCode.RESERVATION_NOT_FOUND,
                    "There is no reservations"
            );
        }
        return convertReservationListToResponse(reservationsList);
    }
    public List<ReservationResponse> convertReservationListToResponse(List<ReservationEntity> reservationList) {
        return reservationList.stream()
                .map(reservationEntity -> convertReservationToResponse(reservationEntity))
                .collect(Collectors.toUnmodifiableList());
    }


    @Override
    public ReservationResponse cancelReservation(Long id) {

        ReservationEntity reservationEntity = reservationRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "Reservation not found"
                ));
        if (isReservationCancelled(id)) {
            throw new ServiceException(
                    ErrorCode.RESERVATION_IS_CANCELLED_OR_ONGOING,
                    "Reservation is already cancelled"
            );
        }

        if (isReservationOngoingOrCompleted(id)) {
            throw new ServiceException(
                    ErrorCode.RESERVATION_IS_CANCELLED_OR_ONGOING,
                    "Cannot cancel an ongoing or completed reservation"
            );
        }


        reservationEntity.setStatus(ReservationStatus.CANCELLED);
        ReservationEntity savedReservation = reservationRepository.save(reservationEntity);

        return convertReservationToResponse(savedReservation);
    }

    @Override
    public List<ReservationResponse> cancelReservationsOfVehicle(Long vehicleId) {

        List<ReservationEntity> vehiclesReservations = reservationRepository.listOfActiveOrFutureReservations(vehicleId, LocalDateTime.now());


        List<ReservationResponse> cancelledReservations = new ArrayList<>();
        for (ReservationEntity reservation : vehiclesReservations) {
            reservation.setStatus(ReservationStatus.CANCELLED);
            ReservationEntity savedReservation = reservationRepository.save(reservation);
            cancelledReservations.add(convertReservationToResponse(savedReservation));
        }

        return cancelledReservations;
    }
    @Override
    public List<ReservationResponse> listOfActiveOrFutureReservations(Long vehicleId) {
        LocalDateTime currentTime = LocalDateTime.now();
        List<ReservationEntity> vehiclesReservations = reservationRepository.listOfActiveOrFutureReservations(vehicleId, currentTime);
        List<ReservationResponse> reservationResponseList = convertReservationListToResponse(vehiclesReservations);

        return reservationResponseList;
    }



    public boolean isReservationOngoingOrCompleted(Long id) {
        LocalDateTime currentTime = LocalDateTime.now();
        return reservationRepository.isReservationOngoingOrCompleted(id,currentTime);
    }
    public boolean isReservationCancelled(Long reservationId) {
        ReservationEntity reservation=reservationRepository.findById(reservationId).get();
        if(reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            return true;
        }
        return false;

    }

    @Override
    public List<ReservationStatisticsResponse> getReservationStatistics(String givenDate) {
        // Parse the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        YearMonth yearMonth = YearMonth.parse(givenDate, formatter);
        String givenMonth = yearMonth.getMonth().toString();
        int givenYear = yearMonth.getYear();

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        if (!reservationRepository.areReservationsForMonth(startOfMonth, endOfMonth)) {
            throw new ServiceException(ErrorCode.RESERVATION_NOT_FOUND,
                    "There are no reservations for the given month");
        }

        LocalDateTime currentTime = LocalDateTime.now();


        ReservationStatisticsProjection completed = reservationRepository.completedReservationsWithStats(startOfMonth, endOfMonth, currentTime);
        ReservationStatisticsProjection ongoing = reservationRepository.ongoingReservationsWithStats(startOfMonth, endOfMonth, currentTime);
        ReservationStatisticsProjection cancelled = reservationRepository.cancelledReservationsWithStats(startOfMonth, endOfMonth);


        ReservationStatisticsResponse completedResponse = ReservationStatisticsResponse.builder()
                .month(givenMonth)
                .year(givenYear)
                .status("COMPLETED")
                .count(completed.getCount() != null ? completed.getCount() : 0)
                .profit(completed.getTotalPrice() != null ? completed.getTotalPrice() : 0.0)
                .build();

        ReservationStatisticsResponse ongoingResponse = ReservationStatisticsResponse.builder()
                .month(givenMonth)
                .year(givenYear)
                .status("ONGOING")
                .count(ongoing.getCount() != null ? ongoing.getCount() : 0)
                .profit(ongoing.getTotalPrice() != null ? ongoing.getTotalPrice() : 0.0)
                .build();

        ReservationStatisticsResponse cancelledResponse = ReservationStatisticsResponse.builder()
                .month(givenMonth)
                .year(givenYear)
                .status("CANCELLED")
                .count(cancelled.getCount() != null ? cancelled.getCount() : 0)
                .profit(cancelled.getTotalPrice() != null ? cancelled.getTotalPrice() : 0.0)
                .build();

        return List.of(completedResponse, ongoingResponse, cancelledResponse);
    }

    @Override
    public List<ReservationResponse> getReservationListOfUser() {
        Long userId=authenticationService.getCurrentUserId();
        List<ReservationEntity> reservationEntityList=reservationRepository.findUsersReservations(userId);
        List<ReservationResponse> reservationResponseList=convertReservationListToResponse(reservationEntityList);
        if(reservationResponseList.isEmpty()) {
            return List.of();
        }
        return reservationResponseList;
    }


}