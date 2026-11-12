package v1.mapaurbano.dto.response;

import lombok.Builder;
import v1.mapaurbano.entity.Ocorrencia;
import v1.mapaurbano.entity.Usuario;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
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
        LocalDateTime dataCricao,
        Usuario usuario
) {
    public static OcorrenciaResponseDTO fromEntity(Ocorrencia ocorrencia){
        if(ocorrencia == null){
            return null;
        }
        return OcorrenciaResponseDTO.builder()
                .id(ocorrencia.getId())
                .descricao(ocorrencia.getDescricao())
                .categoria(String.valueOf(ocorrencia.categoria))
                .logradouro(ocorrencia.getLogradouro())
                .numero(ocorrencia.getNumero())
                .bairro(ocorrencia.getBairro())
                .cidade(ocorrencia.getCidade())
                .longitude(ocorrencia.getLongitude())
                .latitude(ocorrencia.getLatitude())
                .status(String.valueOf(ocorrencia.getStatus()))
                .dataCricao(ocorrencia.getDataCriacao())
                .usuario(ocorrencia.getUsuario())
                .build();
    }
}
