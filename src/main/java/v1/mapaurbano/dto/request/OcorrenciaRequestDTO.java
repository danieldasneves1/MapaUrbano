package v1.mapaurbano.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OcorrenciaRequestDTO(

        @NotBlank(message = "descricao deve ser obrigatoria")
        String descricao,

        @NotBlank(message = "categoria deve ser obrigatoria")
        String categoria,

        @NotBlank(message = "logradouro deve ser obrigatorio")
        String logradoruo,

        @NotBlank(message = "numero deve ser obrigatorio")
        String numero,

        @NotBlank(message = "bairro deve ser obrigatorio")
        String bairro,

        @NotBlank(message = "cidade deve ser obrigatoria")
        String cidade

) {}
