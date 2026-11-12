package v1.mapaurbano.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import v1.mapaurbano.dto.request.UsuarioRequestDTO;
import v1.mapaurbano.dto.response.TokenResponseDTO;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.repository.UsuarioRepository;

import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static v1.mapaurbano.entity.enums.Role.ROLE_USER;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void criarUsuario(){
        UsuarioRequestDTO usuarioParaSalvar = new UsuarioRequestDTO("daniel2", "da@gmail.com","12345679");
        Usuario usuarioSalvo = new Usuario(2L, "daniel2", "da@gmail.com", "12345679", ROLE_USER);


        when(passwordEncoder.encode(any(String.class))).thenReturn("12345679");
        when(usuarioRepository.findByEmail("da@gmail.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        TokenResponseDTO resultado = usuarioService.cadastrar(usuarioParaSalvar);

        assertThat(resultado).isNotNull();
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }
}
