package v1.mapaurbano.dto.response;

import lombok.Builder;

@Builder
public record TokenResponseDTO(

        String token,
        String tipo,
        Long expiresAt,
        String nome,
        String role
) {}
