package v1.mapaurbano.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        @NotBlank(message = "O nome e obrigatorio")
        String nome,

        @NotBlank(message = "O email deve ser obrigatorio")
        String email,

        @NotBlank(message = "a senha deve ser obrigatoria")
        @Size(min = 6, message = "a senha deve ter no minimo 6 caracteres")
        String senha

) {}
