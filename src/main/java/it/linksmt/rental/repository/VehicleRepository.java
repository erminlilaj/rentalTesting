package it.linksmt.rental.repository;

import it.linksmt.rental.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    @Query("SELECT v FROM VehicleEntity v WHERE v.deletedAt IS NULL")
    List<VehicleEntity> findCurrentVehicles();

}
