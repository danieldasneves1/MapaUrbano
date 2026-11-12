package v1.mapaurbano.service;

import jakarta.transaction.Status;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import v1.mapaurbano.config.TokenProvider;
import v1.mapaurbano.dto.request.LoginRequestDTO;
import v1.mapaurbano.dto.request.UsuarioRequestDTO;
import v1.mapaurbano.dto.response.TokenResponseDTO;
import v1.mapaurbano.dto.response.UsuarioResponseDTO;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.entity.enums.Role;
import v1.mapaurbano.exception.GlobalExceptionHandler;
import v1.mapaurbano.repository.UsuarioRepository;

import javax.security.auth.login.CredentialException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;


    //registrar usuario
    public TokenResponseDTO cadastrar(UsuarioRequestDTO usuarioRequestDTO){
            if(usuarioRepository.findByEmail(usuarioRequestDTO.email()).isPresent()){
                throw new GlobalExceptionHandler("email do usuario ja cadastrado");
            }

            Usuario usuario = Usuario.builder()
                    .nome(usuarioRequestDTO.nome())
                    .email(usuarioRequestDTO.email())
                    .senha(passwordEncoder.encode(usuarioRequestDTO.senha()))
                    .role(Role.ROLE_USER)
                    .build();

                usuario = usuarioRepository.save(usuario);

                String token = tokenProvider.gerarToken(usuario);

                return TokenResponseDTO.builder()
                        .token(token)
                        .tipo("Bearer")
                        .expiresAt(tokenProvider.getExpireAt())
                        .nome(usuarioRequestDTO.nome())
                        .role("ROLE_USER")
                        .build();

    }

   //logar usuario
    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO){
        try {
            String emailNormalizado = loginRequestDTO.email() != null ? loginRequestDTO.email().trim().toLowerCase() : "";
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(emailNormalizado, loginRequestDTO.senha())
            );

            Usuario usuario = (Usuario) authentication.getPrincipal();

            String token = tokenProvider.gerarToken(usuario);

            return TokenResponseDTO.builder()
                    .token(token)
                    .tipo("Bearer ")
                    .expiresAt(tokenProvider.getExpireAt() / 1000)
                    .nome(usuario.getNome())
                    .role(usuario.getRole().name())
                    .build();
        }catch (GlobalExceptionHandler e){
            throw new GlobalExceptionHandler("credenciais invalidas");
        }
    }

    //buscar usuario por email(somente para admin)
    public UsuarioResponseDTO buscarUsuario(String email){
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalExceptionHandler("usuario nao encontrado"));

        return UsuarioResponseDTO.fromEntity(usuario);
    }

    //buscar todos usuario(somente admin)
    public List<UsuarioResponseDTO> buscarUsuarios(){
        List<Usuario> usuarios = usuarioRepository.findAll();

        if(usuarios == null){
            throw new GlobalExceptionHandler("ainda nao existem usuarios");
        }

        return usuarios.stream()
                .map(UsuarioResponseDTO::fromEntity)
                .toList();
    }

    //atualizar perfil de usuario
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO usuarioRequestDTO){
        if(usuarioRequestDTO.email() == null || usuarioRepository.existsById(id) == false){
            throw new UsernameNotFoundException("usuario nao encontrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(usuarioRequestDTO.nome())
                .email(usuarioRequestDTO.email())
                .build();

        usuario = usuarioRepository.save(usuario);

        return UsuarioResponseDTO.fromEntity(usuario);

    }

    //deletar perfil de usuario
    public void deletar(Long id){
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler("usuario nao encontrado"));

        usuarioRepository.deleteById(id);
    }
}
