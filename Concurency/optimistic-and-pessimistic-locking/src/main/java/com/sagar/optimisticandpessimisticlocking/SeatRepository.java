package com.sagar.optimisticandpessimisticlocking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeatRepository extends JpaRepository<Seat,Long> {

    @Query(value = "select * from seats where id = :id for update",nativeQuery = true)
    Seat findAndLockSeat(@Param("id")Long id);
}
