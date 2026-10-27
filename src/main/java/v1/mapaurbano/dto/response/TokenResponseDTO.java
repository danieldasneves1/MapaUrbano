package v1.mapaurbano.dto.response;

public record TokenResponseDTO(

        String token,
        String tipo,
        Long expiresAt,
        String nome,
        String role
) {}
