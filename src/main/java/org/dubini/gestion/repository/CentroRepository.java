package org.dubini.gestion.repository;

import org.dubini.gestion.model.Centro;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroRepository extends ListCrudRepository<Centro, Long>, PagingAndSortingRepository<Centro, Long> {

    @Query("SELECT COUNT(*) FROM miembros WHERE CENTRO_ID = :centroId")
    long countMiembrosByCentroId(@Param("centroId") Long centroId);
}
