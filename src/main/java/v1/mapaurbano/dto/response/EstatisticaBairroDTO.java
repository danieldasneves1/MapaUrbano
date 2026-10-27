package v1.mapaurbano.dto.response;

import java.util.Map;

public record EstatisticaBairroDTO(

    Long totalOcorrencias,
    Long totalPendentes,
    Long totalEmAndamento,
    Long totalResolvidas,
    Long totalCanceladas,

    Map<String, Long> ocorrenciaPorCategoria,

    Map<String, Long> ocorrenciaPorBairro,

    Double percentualResolvidos

) {}
