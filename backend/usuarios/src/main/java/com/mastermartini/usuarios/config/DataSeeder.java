package com.mastermartini.usuarios.config;

import com.mastermartini.usuarios.model.Rol;
import com.mastermartini.usuarios.model.Usuario;
import com.mastermartini.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Carga los usuarios iniciales en la base de datos si aún no existen registros.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Inserta en orden los 4 usuarios de demostración con contraseña encriptada.
    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            String passwordEncriptada = passwordEncoder.encode("Demo1234");
            usuarioRepository.saveAll(List.of(
                    new Usuario("Carlos Mendoza", "11111111-1", "cliente@example.com", "912345678",
                            "Panadería", "2 a 5 personas", passwordEncriptada, Rol.CLIENTE, false),
                    new Usuario("Chef Marco Rossi", "22222222-2", "instructor@example.com", "923456789",
                            "Chocolatería", "Master Martini", passwordEncriptada, Rol.INSTRUCTOR, true),
                    new Usuario("Sofía Valdés", "15482930-K", "ventas@example.com", "934567890",
                            "Comercial B2B", "Master Martini", passwordEncriptada, Rol.VENTAS, true),
                    new Usuario("Admin Sistema", "18765432-7", "admin@example.com", "945678901",
                            "Administración", "Master Martini", passwordEncriptada, Rol.ADMINISTRADOR, true)
            ));
        }
    }
}
