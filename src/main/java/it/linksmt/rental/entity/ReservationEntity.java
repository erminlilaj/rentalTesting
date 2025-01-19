package it.linksmt.rental.entity;

import it.linksmt.rental.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "reservations")
@AllArgsConstructor
@NoArgsConstructor

public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private double totalPrice;
    private int durationDays;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public ReservationEntity(Long id, VehicleEntity vehicle, UserEntity user, LocalDateTime startDate, LocalDateTime endDate, ReservationStatus status, int duration, double price) {
        this.id = id;
        this.vehicle = vehicle;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.durationDays = duration;
        this.totalPrice = price;
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
