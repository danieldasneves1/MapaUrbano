package v1.mapaurbano.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(

        @NotBlank(message = "email deve ser obrigatorio")
        String email,

        @NotBlank(message = "senha deve ser obrigatoria")
        @Size(min = 6, message = "a senha deve ter no minimo 6 caracteres")
        String senha

) {}
