package petcare.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "animais")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 80)
    private String especie;

    @Column(length = 100)
    private String raca;

    @Column(name = "nascimento_estimado")
    private LocalDate nascimentoEstimado;

    @Column(length = 20)
    private String sexo;

    @Column(length = 2000)
    private String observacoes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaca() { return raca; }
    public void setRaca(String raca) { this.raca = raca; }
    public LocalDate getNascimentoEstimado() { return nascimentoEstimado; }
    public void setNascimentoEstimado(LocalDate n) { this.nascimentoEstimado = n; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}