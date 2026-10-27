package v1.mapaurbano.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AtualizarStatusDTO(

        @NotBlank
        String novoStatus

) {}
