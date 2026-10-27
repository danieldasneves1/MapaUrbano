package v1.mapaurbano.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import v1.mapaurbano.dto.request.UsuarioRequestDTO;
import v1.mapaurbano.dto.response.TokenResponseDTO;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;


    public TokenResponseDTO registrar(UsuarioRequestDTO usuario){
       Usuario usuario1  = usuarioRepository.

    }

}
