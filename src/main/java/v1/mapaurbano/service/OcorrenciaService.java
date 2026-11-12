package v1.mapaurbano.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import v1.mapaurbano.dto.request.OcorrenciaRequestDTO;
import v1.mapaurbano.dto.response.OcorrenciaResponseDTO;
import v1.mapaurbano.entity.Ocorrencia;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.entity.enums.Categoria;
import v1.mapaurbano.entity.enums.StatusOcorrencia;
import v1.mapaurbano.exception.GlobalExceptionHandler;
import v1.mapaurbano.repository.OcorrenciaRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OcorrenciaService {

    public final OcorrenciaRepository ocorrenciaRepository;

    //registrar ocorrencia
    public OcorrenciaResponseDTO criar(OcorrenciaRequestDTO ocorrenciaRequestDTO, @AuthenticationPrincipal Usuario usuario){
        if(usuario == null){
            throw new GlobalExceptionHandler("usuario nao encontrado");
        }

        Ocorrencia ocorrencia = Ocorrencia.builder()
                .descricao(ocorrenciaRequestDTO.descricao())
                .categoria(Categoria.valueOf(ocorrenciaRequestDTO.categoria()))
                .logradouro(ocorrenciaRequestDTO.logradouro())
                .numero(ocorrenciaRequestDTO.numero())
                .bairro(ocorrenciaRequestDTO.bairro())
                .cidade(ocorrenciaRequestDTO.cidade())
                .status(StatusOcorrencia.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .usuario(usuario)
                .build();

         ocorrencia = ocorrenciaRepository.save(ocorrencia);

        return OcorrenciaResponseDTO.fromEntity(ocorrencia);
    }

    //buscar ocorrencias de um usuario expecifico
    public List<OcorrenciaResponseDTO> buscar(@AuthenticationPrincipal Usuario usuario){
        List<Ocorrencia> ocorrencias= ocorrenciaRepository.findByUsuarioId(usuario.getId());

        return ocorrencias.stream()
                .map(OcorrenciaResponseDTO::fromEntity)
                .toList();
    }

    //atualizar ocorrencia
    public OcorrenciaResponseDTO atualizar(Long id, OcorrenciaRequestDTO ocorrenciaRequestDTO, @AuthenticationPrincipal Usuario usuario){
        if(id == null) {
            throw new GlobalExceptionHandler("ocorrencia nao encontrada");
        }
        if(usuario == null){
            throw new GlobalExceptionHandler("usuario nao encontrado");
        }

        Ocorrencia ocorrencia = Ocorrencia.builder()
                .descricao(ocorrenciaRequestDTO.descricao())
                .categoria(Categoria.valueOf(ocorrenciaRequestDTO.categoria()))
                .logradouro(ocorrenciaRequestDTO.logradouro())
                .numero(ocorrenciaRequestDTO.numero())
                .bairro(ocorrenciaRequestDTO.bairro())
                .cidade(ocorrenciaRequestDTO.cidade())
                .status(StatusOcorrencia.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .usuario(usuario)
                .build();

        ocorrencia = ocorrenciaRepository.save(ocorrencia);

        return OcorrenciaResponseDTO.fromEntity(ocorrencia);
    }

    //deletar ocorrencia
    public void deletar(Long id){
        if(id == null){
            throw new GlobalExceptionHandler("ocorrencia nao encontrada");
        }

        ocorrenciaRepository.deleteById(id);
    }


}
