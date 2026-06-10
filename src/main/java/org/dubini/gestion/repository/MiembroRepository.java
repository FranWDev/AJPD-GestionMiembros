package org.dubini.gestion.repository;

import org.dubini.gestion.model.Miembro;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MiembroRepository extends ListCrudRepository<Miembro, Long>, PagingAndSortingRepository<Miembro, Long>, MiembroRepositoryCustom {
}
