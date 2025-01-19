package it.linksmt.rental.entity;

import it.linksmt.rental.enums.FuelType;
import it.linksmt.rental.enums.GearboxType;
import it.linksmt.rental.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "vehicles")
@AllArgsConstructor
@NoArgsConstructor
//@SQLRestriction("deleted_at is NULL")
public class VehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String brand;
    private String model;
    private int year;
    @Enumerated(EnumType.STRING)
    private GearboxType gearboxType;
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;
    private String color;
    @Enumerated(EnumType.STRING)
    private VehicleStatus vehicleStatus;

    private double dailyFee;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public VehicleEntity(Long id, String brand, String model, VehicleStatus vehicleStatus) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.vehicleStatus = vehicleStatus;
    }


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


}
