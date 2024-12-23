package it.linksmt.rental.repository.projections;

public interface ReservationStatisticsProjection {
    Integer getCount();
    Double getTotalPrice();
}
