package org.dubini.gestion.repository;

import org.dubini.gestion.model.Cargo;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoRepository extends ListCrudRepository<Cargo, Long>, PagingAndSortingRepository<Cargo, Long> {

    @Query("SELECT COUNT(*) FROM miembros WHERE CARGO_ID = :cargoId")
    long countMiembrosByCargoId(@Param("cargoId") Long cargoId);

    @Query("SELECT COUNT(*) FROM historial_cargos WHERE CARGO_ID = :cargoId")
    long countHistorialByCargoId(@Param("cargoId") Long cargoId);
}
