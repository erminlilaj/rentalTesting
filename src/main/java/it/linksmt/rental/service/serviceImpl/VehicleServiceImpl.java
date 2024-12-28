package it.linksmt.rental.service.serviceImpl;

import it.linksmt.rental.dto.CreateVehicleRequest;
import it.linksmt.rental.dto.UpdateVehicleRequest;
import it.linksmt.rental.dto.VehicleResponse;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.ErrorCode;

import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.VehicleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    VehicleRepository vehicleRepository;
    public AuthenticationService authenticationService;

    public VehicleServiceImpl(VehicleRepository vehicleRepository, AuthenticationService authenticationService) {
        this.vehicleRepository = vehicleRepository;
        this.authenticationService = authenticationService;

    }

    @Override
    public VehicleEntity createVehicle(CreateVehicleRequest createVehicleRequest) {

        if (!authenticationService.isAdmin()) {

            throw new ServiceException(
                    ErrorCode.UNAUTHORIZED_ACCESS,
                    "You do not have access to create a vehicle");
        }
        if (createVehicleRequest.getYear() < 1980) {
            throw new ServiceException(ErrorCode.BAD_VEHICLE_DETAILS,
                    "The year must be newer than 1980");
        }
        try {
            VehicleEntity vehicleEntity = new VehicleEntity();
            vehicleEntity.setBrand(createVehicleRequest.getBrand());
            vehicleEntity.setModel(createVehicleRequest.getModel());
            vehicleEntity.setYear(createVehicleRequest.getYear());
            vehicleEntity.setGearboxType(createVehicleRequest.getGearboxType());
            vehicleEntity.setFuelType(createVehicleRequest.getFuelType());
            vehicleEntity.setColor(createVehicleRequest.getColor());
            vehicleEntity.setVehicleStatus(createVehicleRequest.getVehicleStatus());
            vehicleEntity.setDailyFee(createVehicleRequest.getDailyFee());

            return vehicleRepository.save(vehicleEntity);
        } catch (Exception e) {
            throw new ServiceException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occured while creating the vehicle");
        }
    }

    @Override
    public List<VehicleEntity> findAllVehicle() {
        if (vehicleRepository.findAll().isEmpty()) {
            throw new ServiceException(
                    ErrorCode.VEHICLE_NOT_FOUND,
                    "No vehicle found");
        }
        try {

            return vehicleRepository.findCurrentVehicles();
        } catch (Exception e) {
            throw new ServiceException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occured while finding all vehicles");
        }
    }

    @Override
    public VehicleResponse findVehicleById(Long id) {

        VehicleEntity vehicleEntity = vehicleRepository.findById(id).orElseThrow(() -> new ServiceException(
                ErrorCode.VEHICLE_NOT_FOUND));
        return convertVehicleToResponse(vehicleEntity);
    }

    public VehicleEntity getVehicleById(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ServiceException(
                ErrorCode.VEHICLE_NOT_FOUND));
    }

    private VehicleResponse convertVehicleToResponse(VehicleEntity vehicleEntity) {
        VehicleResponse vehicleResponse = new VehicleResponse();
        vehicleResponse.setId(vehicleEntity.getId());
        vehicleResponse.setBrand(vehicleEntity.getBrand());
        vehicleResponse.setModel(vehicleEntity.getModel());
        vehicleResponse.setYear(vehicleEntity.getYear());
        vehicleResponse.setGearboxType(vehicleEntity.getGearboxType());
        vehicleResponse.setFuelType(vehicleEntity.getFuelType());
        vehicleResponse.setColor(vehicleEntity.getColor());
        vehicleResponse.setVehicleStatus(vehicleEntity.getVehicleStatus());
        vehicleResponse.setDailyFee(vehicleEntity.getDailyFee());
        return vehicleResponse;
    }

    @Override
    public boolean deleteVehicle(Long id) {
        // SecurityBean currentUser = SecurityContext.get();

        if (!authenticationService.isAdmin()) {
            throw new ServiceException(
                    ErrorCode.UNAUTHORIZED_ACCESS,
                    "You do not have access to delete a vehicle");
        }
        if (!vehicleRepository.existsById(id)) {
            throw new ServiceException(
                    ErrorCode.VEHICLE_NOT_FOUND,
                    "No vehicle found with id: " + id);
        }
        try {
            // perform soft delete
            VehicleEntity deletedVehicle = vehicleRepository.findById(id).orElse(null);
            if (deletedVehicle == null) {
                throw new ServiceException(ErrorCode.VEHICLE_NOT_FOUND,
                        "Vehicle not found for deletion with id: " + id);
            }
            deletedVehicle.setDeletedAt(LocalDateTime.now());
            // if(vehicleBusinessLayer.activateOrFutureReservationOfVehicle(id)!=null){
            // vehicleBusinessLayer.deleteVehicle(id);
            // }

            vehicleRepository.save(deletedVehicle);
            return true;
        } catch (Exception e) {
            throw new ServiceException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occured while deleting a vehicle");
        }

    }

    @Override
    public VehicleEntity updateVehicle(Long id, UpdateVehicleRequest updateVehicleRequest) {
        // SecurityBean currentUser = SecurityContext.get();

        if (!authenticationService.isAdmin()) {
            throw new ServiceException(
                    ErrorCode.UNAUTHORIZED_ACCESS,
                    "You do not have access to update a vehicle.");
        }
        // todo if null
        VehicleEntity vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                        ErrorCode.VEHICLE_NOT_FOUND,
                        "No vehicle found with id: " + id));
        vehicle.setModel(updateVehicleRequest.getModel());
        vehicle.setColor(updateVehicleRequest.getColor());
        vehicle.setDailyFee(updateVehicleRequest.getDailyFee());
        vehicle.setVehicleStatus(updateVehicleRequest.getVehicleStatus());
        return vehicleRepository.save(vehicle);
    }

    @Override
    public double getVehiclePrice(Long id) {
        VehicleEntity vehicle = getVehicleById(id);
        return vehicle.getDailyFee();
    }

}
