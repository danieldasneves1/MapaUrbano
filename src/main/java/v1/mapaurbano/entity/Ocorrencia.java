package v1.mapaurbano.entity;

import jakarta.persistence.*;
import lombok.*;
import v1.mapaurbano.entity.enums.Categoria;
import v1.mapaurbano.entity.enums.StatusOcorrencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ocorrencia")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", nullable = false)
    public String descricao;

    @Column(name = "categoria")
    @Enumerated(EnumType.STRING)
    public Categoria categoria;

    @Column(name = "logradouro")
    private String logradouro;

    @Column(name = "numero")
    private String numero;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "cidade")
    private String cidade;

    @Column(name = "longitude")
    public BigDecimal longitude;

    @Column(name = "latitude")
    public BigDecimal latitude;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    public StatusOcorrencia status;

    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @PrePersist
    protected void aocriar(){
        this.dataCriacao = LocalDateTime.now();
        if(this.cidade == null){
            this.cidade = "Belem";
        }
        if(this.bairro == null){
            this.bairro = "Marco";
        }
    }
}
