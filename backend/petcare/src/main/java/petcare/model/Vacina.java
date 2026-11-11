package petcare.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vacinas")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "data_aplicacao", nullable = false)
    private LocalDate dataAplicacao;

    @Column(name = "proxima_data")
    private LocalDate proximaData;

    @Column(length = 2000)
    private String observacoes;

    public Long getId() { return id; }
    public Animal getAnimal() { return animal; }
    public void setAnimal(Animal animal) { this.animal = animal; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate d) { this.dataAplicacao = d; }
    public LocalDate getProximaData() { return proximaData; }
    public void setProximaData(LocalDate p) { this.proximaData = p; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String o) { this.observacoes = o; }
}