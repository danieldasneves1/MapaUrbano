package v1.mapaurbano.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import v1.mapaurbano.dto.request.OcorrenciaRequestDTO;
import v1.mapaurbano.dto.response.OcorrenciaResponseDTO;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.service.OcorrenciaService;

import java.util.List;

@RestController
@RequestMapping("/ocorrencias")
@RequiredArgsConstructor
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService;

    @PostMapping("/registrar")
    public ResponseEntity<OcorrenciaResponseDTO> registrarOcorrencia(@RequestBody OcorrenciaRequestDTO ocorrenciaRequestDTO,
                                                                     @AuthenticationPrincipal Usuario usuario){
        OcorrenciaResponseDTO ocorrencia= ocorrenciaService.criar(ocorrenciaRequestDTO, usuario);
        return ResponseEntity.ok(ocorrencia);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<OcorrenciaResponseDTO>> listarOcorrenciasPorId(@AuthenticationPrincipal Usuario usuario){
        List<OcorrenciaResponseDTO> ocorrencias = ocorrenciaService.buscar(usuario);

        return ResponseEntity.ok(ocorrencias);
    }

    @PutMapping("/atualizar")
    public ResponseEntity<OcorrenciaResponseDTO> atualizar(@PathVariable Long id,
                                                           @RequestBody OcorrenciaRequestDTO ocorrenciaRequestDTO,
                                                           @AuthenticationPrincipal Usuario usuario){
        OcorrenciaResponseDTO ocorrencia = ocorrenciaService.atualizar(id, ocorrenciaRequestDTO, usuario);

        return ResponseEntity.ok(ocorrencia);
    }

    @DeleteMapping
    public void deletar(@PathVariable Long id){
        ocorrenciaService.deletar(id);

        ResponseEntity.noContent().build();
    }
}
