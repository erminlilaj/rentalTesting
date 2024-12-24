package it.linksmt.rental.controller;

import it.linksmt.rental.dto.CreateVehicleRequest;
import it.linksmt.rental.dto.UpdateVehicleRequest;
import it.linksmt.rental.dto.VehicleResponse;
import it.linksmt.rental.entity.VehicleEntity;

import it.linksmt.rental.exception.ServiceException;
import it.linksmt.rental.service.VehicleBusinessLayer;
import it.linksmt.rental.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/vehicles")
//@CrossOrigin
public class VehicleController {

    private final VehicleService vehicleService;

    private final VehicleBusinessLayer vehicleBusinessLayer;

    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createVehicle(@RequestBody CreateVehicleRequest createVehicleRequest) {
        try {
            VehicleEntity createdVehicle = vehicleService.createVehicle(createVehicleRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
        } catch (ServiceException e) {
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<VehicleEntity>> getAllVehicles() {
        try {
            List<VehicleEntity> vehicleList = vehicleService.findAllVehicle();

            return ResponseEntity.status(HttpStatus.OK).body(vehicleList);
        }catch (ServiceException e) {
            throw e;
        }
    }


    @GetMapping(produces =MediaType.APPLICATION_JSON_VALUE,path = {"/{id}"})
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {

            VehicleResponse vehicle = vehicleService.findVehicleById(id);

            return ResponseEntity.status(HttpStatus.OK).body(vehicle);


    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        try{
     vehicleBusinessLayer.deleteVehicle(id);
            return ResponseEntity.status(HttpStatus.OK).build();
        }catch (ServiceException e) {
            throw e;
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleEntity> updateVehicle(@PathVariable Long id,@Valid @RequestBody UpdateVehicleRequest updateVehicleRequest) {

            VehicleEntity vehicle = vehicleService.updateVehicle(id, updateVehicleRequest);

            return ResponseEntity.status(HttpStatus.OK).body(vehicle);

}

}



