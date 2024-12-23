package it.linksmt.rental.service;

import it.linksmt.rental.dto.CreateVehicleRequest;
import it.linksmt.rental.dto.UpdateVehicleRequest;
import it.linksmt.rental.dto.VehicleResponse;
import it.linksmt.rental.entity.VehicleEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface VehicleService {
    VehicleEntity createVehicle(CreateVehicleRequest createVehicleRequest, MultipartFile image);

    List<VehicleEntity> findAllVehicle();

    VehicleResponse findVehicleById(Long id);

    boolean deleteVehicle(Long id);

    VehicleEntity updateVehicle(Long id, UpdateVehicleRequest updateVehicleRequest);

    double getVehiclePrice(Long id);

    Resource getVehicleImage(String imagePath)throws IOException;;
}
