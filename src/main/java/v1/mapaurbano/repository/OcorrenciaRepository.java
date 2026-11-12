package v1.mapaurbano.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import v1.mapaurbano.entity.Ocorrencia;
import v1.mapaurbano.entity.Usuario;
import v1.mapaurbano.entity.enums.StatusOcorrencia;

import java.util.List;
import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

    List<Ocorrencia> findByStatus(StatusOcorrencia status);

    @Query("SELECT nome.categoria, COUNT(nome) FROM Ocorrencia nome GROUP BY nome.categoria")
    List<Object[]> contarOcorrenciaPorCategoria();

    @Query("SELECT nome.bairro, COUNT(nome) FROM Ocorrencia nome GROUP BY nome.bairro")
    List<Object[]> contarOcorrenciaPorBairro();

    List<Ocorrencia> findByUsuarioId(Long id);
}
