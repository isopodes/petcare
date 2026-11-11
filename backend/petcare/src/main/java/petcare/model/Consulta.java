package petcare.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(length = 150)
    private String profissional;

    @Column(length = 150)
    private String clinica;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 2000)
    private String motivo;

    @Column(length = 2000)
    private String observacoes;

    public Long getId() { return id; }
    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }
    public String getProfissional() { return profissional; }
    public void setProfissional(String p) { this.profissional = p; }
    public String getClinica() { return clinica; }
    public void setClinica(String c) { this.clinica = c; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime d) { this.dataHora = d; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String m) { this.motivo = m; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String o) { this.observacoes = o; }
}