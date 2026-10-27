package v1.mapaurbano.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import v1.mapaurbano.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{

    Optional<Usuario> findByEmail(String email);
}
