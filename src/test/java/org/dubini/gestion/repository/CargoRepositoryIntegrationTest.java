package org.dubini.gestion.repository;

import org.dubini.gestion.model.Cargo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CargoRepositoryIntegrationTest {

    @Autowired
    private CargoRepository cargoRepository;

    @Test
    void testSaveAndFindCargo() {
        Cargo cargo = new Cargo(null, "Presidente Test");
        Cargo saved = cargoRepository.save(cargo);

        assertNotNull(saved.getId());
        assertEquals("Presidente Test", saved.getNombre());

        Optional<Cargo> found = cargoRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Presidente Test", found.get().getNombre());
    }
}
