package v1.mapaurbano.dto.response;

import lombok.Builder;
import v1.mapaurbano.entity.Usuario;

import java.nio.charset.StandardCharsets;

@Builder
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String role

) {

    public static UsuarioResponseDTO fromEntity(Usuario usuario){
       if(usuario == null){
           return null;
       }
       return UsuarioResponseDTO.builder()
               .id(usuario.getId())
               .nome(usuario.getNome())
               .email(usuario.getEmail())
               .role(String.valueOf(usuario.getRole()))
               .build();
    }

}
