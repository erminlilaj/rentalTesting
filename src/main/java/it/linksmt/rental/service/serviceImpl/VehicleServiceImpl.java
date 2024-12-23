package it.linksmt.rental.service.serviceImpl;

import it.linksmt.rental.dto.CreateVehicleRequest;
import it.linksmt.rental.dto.ReservationResponse;
import it.linksmt.rental.dto.UpdateVehicleRequest;
import it.linksmt.rental.dto.VehicleResponse;
import it.linksmt.rental.entity.VehicleEntity;
import it.linksmt.rental.enums.ErrorCode;

import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.repository.VehicleRepository;
import it.linksmt.rental.service.AuthenticationService;
import it.linksmt.rental.service.FileStorageService;
import it.linksmt.rental.service.VehicleBusinessLayer;
import it.linksmt.rental.service.VehicleService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    VehicleRepository vehicleRepository;
    public AuthenticationService authenticationService;
    public FileStorageService fileStorageService;


    public VehicleServiceImpl(VehicleRepository vehicleRepository, AuthenticationService authenticationService, FileStorageService fileStorageService) {
        this.vehicleRepository = vehicleRepository;
        this.authenticationService = authenticationService;
        this.fileStorageService = fileStorageService;
        //this.vehicleBusinessLayer = vehicleBusinessLayer;

    }

    @Override
    public VehicleEntity createVehicle(CreateVehicleRequest createVehicleRequest, MultipartFile image) {
        //SecurityBean currentUser = SecurityContext.get();

        if (!authenticationService.isAdmin()) {
            //throw new AccessDeniedException(
            throw new ServiceException(
                    ErrorCode.UNAUTHORIZED_ACCESS,
                            "You do not have access to create a vehicle"
                    );
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
            //
            if(image!=null && !image.isEmpty()) {
                String imagePath = fileStorageService.storeFile(image);
                vehicleEntity.setImagePath(imagePath);
            }
            return vehicleRepository.save(vehicleEntity);
        }
        catch (Exception e) {
            throw new ServiceException(
            ErrorCode.INTERNAL_SERVER_ERROR,
            "Error occured while creating the vehicle"
            );
        }
    }

    @Override
    public List<VehicleEntity> findAllVehicle() {
        if(vehicleRepository.findAll().isEmpty()) {
            throw new ServiceException(
                    ErrorCode.VEHICLE_NOT_FOUND,
                    "No vehicle found"
            );
        }
        try {

            return vehicleRepository.findCurrentVehicles();
        }catch (Exception e) {
            throw new ServiceException(
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    "Error occured while finding all vehicles"
            );
        }
    }

    @Override
    public VehicleResponse findVehicleById(Long id) {


            VehicleEntity vehicleEntity= vehicleRepository.findById(id).orElseThrow(()->new ServiceException(
                    ErrorCode.VEHICLE_NOT_FOUND
            ));
return convertVehicleToResponse(vehicleEntity);
    }
public VehicleEntity getVehicleById(Long id) {
        return vehicleRepository.findById(id).orElseThrow(()->new ServiceException(
                ErrorCode.VEHICLE_NOT_FOUND
        ));
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
                    "You do not have access to delete a vehicle"
            );
        }
       if(!vehicleRepository.existsById(id)) {
           throw new ServiceException(
                   ErrorCode.VEHICLE_NOT_FOUND,
                   "No vehicle found with id: " + id
           );
       }
       try{
           //perform soft delete
        VehicleEntity deletedVehicle=vehicleRepository.findById(id).orElse(null);
        deletedVehicle.setDeletedAt(LocalDateTime.now());
//if(vehicleBusinessLayer.activateOrFutureReservationOfVehicle(id)!=null){
//    vehicleBusinessLayer.deleteVehicle(id);
//}

        vehicleRepository.save(deletedVehicle);
          return true;
       }catch (Exception e) {
           throw new ServiceException(
                   ErrorCode.INTERNAL_SERVER_ERROR,
                   "Error occured while deleting a vehicle"
           );
       }

    }

    @Override
    public VehicleEntity updateVehicle(Long id, UpdateVehicleRequest updateVehicleRequest) {
        //SecurityBean currentUser = SecurityContext.get();


        if (!authenticationService.isAdmin()) {
            throw new ServiceException(
                    ErrorCode.UNAUTHORIZED_ACCESS,
                    "You do not have access to update a vehicle."
            );
        }
//todo if null
        VehicleEntity vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ServiceException(
                        ErrorCode.VEHICLE_NOT_FOUND,
                        "No vehicle found with id: " + id
                ));
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

    @Override
    public Resource getVehicleImage(String imagePath) throws IOException {
       if(imagePath==null || imagePath.isEmpty()) {
           return null;
       }
       return fileStorageService.getImage(imagePath);
    }

}
