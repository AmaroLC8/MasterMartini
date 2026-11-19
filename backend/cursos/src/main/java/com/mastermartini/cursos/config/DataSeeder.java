package com.mastermartini.cursos.config;

import com.mastermartini.cursos.model.Curso;
import com.mastermartini.cursos.repository.CursoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

// Carga los cursos iniciales en la base de datos si aún no existen registros.
@Component
public class DataSeeder implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    public DataSeeder(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    // Inserta los 3 cursos iniciales de Master Martini cuando la tabla está vacía.
    @Override
    public void run(String... args) {
        if (cursoRepository.count() == 0) {
            cursoRepository.saveAll(List.of(
                    new Curso(
                            "Curva de Templado en Chocolatería",
                            "Chocolatería",
                            "Cobertura Caravella Dark 54%",
                            "Chef Roberto Valenzuela",
                            "15 min",
                            "Básico",
                            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                            false
                    ),
                    new Curso(
                            "Estabilización y Batido de Cremas",
                            "Cremas",
                            "Crema Vegetal Decor Up",
                            "Chef Elena Rossi",
                            "20 min",
                            "Básico",
                            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                            false
                    ),
                    new Curso(
                            "Laminado de Masa de Hoja Artesanal",
                            "Masas",
                            "Margarina Master Martini Placa",
                            "Maestro Marco Zanetti",
                            "35 min",
                            "Intermedio",
                            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
                            true
                    )
            ));
        }
    }
}
