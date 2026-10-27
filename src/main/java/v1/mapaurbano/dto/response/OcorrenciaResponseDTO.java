package v1.mapaurbano.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OcorrenciaResponseDTO(

        Long id,
        String descricao,
        String categoria,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        BigDecimal longitude,
        BigDecimal latitude,
        String status,
        LocalDateTime dataCricao
) {}
