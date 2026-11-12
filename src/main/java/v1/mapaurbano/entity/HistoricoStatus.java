package v1.mapaurbano.entity;

import jakarta.persistence.*;
import lombok.*;
import v1.mapaurbano.entity.enums.StatusOcorrencia;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "HistoricoStatus")
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_anterior")
    @Enumerated(EnumType.STRING)
    public StatusOcorrencia statusAnterior;

    @Column(name = "status_novo")
    @Enumerated(EnumType.STRING)
    public StatusOcorrencia statusNovo;

    @Column(name = "data_alteracao")
    public LocalDateTime dataAlteracao;

    @ManyToOne
    @JoinColumn(name = "ocorrencia_id")
    public Ocorrencia ocorrencia;

    @PrePersist
    protected void aoCriar(){
        this.dataAlteracao = LocalDateTime.now();
    }
}
