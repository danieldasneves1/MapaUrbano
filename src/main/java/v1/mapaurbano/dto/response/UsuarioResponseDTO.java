package v1.mapaurbano.dto.response;

public record UsuarioResponseDTO(

        Long id,
        String nome,
        String email,
        String role
) {}
