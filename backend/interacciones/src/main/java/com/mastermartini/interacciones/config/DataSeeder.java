package com.mastermartini.interacciones.config;

import com.mastermartini.interacciones.model.Interaccion;
import com.mastermartini.interacciones.repository.InteraccionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Carga las interacciones iniciales de prueba si la tabla aún no tiene registros.
@Component
public class DataSeeder implements CommandLineRunner {

    private final InteraccionRepository interaccionRepository;

    public DataSeeder(InteraccionRepository interaccionRepository) {
        this.interaccionRepository = interaccionRepository;
    }

    // Inserta 2 interacciones de prueba para el usuario ID 1 cuando la tabla está vacía.
    @Override
    public void run(String... args) {
        if (interaccionRepository.count() == 0) {
            interaccionRepository.saveAll(List.of(
                    new Interaccion(1L, 1L, true, true, "Templar cobertura Caravella a 31 grados"),
                    new Interaccion(1L, 2L, true, false, "Probar con crema Decor Up fría")
            ));
        }
    }
}
