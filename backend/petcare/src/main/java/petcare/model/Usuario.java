package petcare.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 180)
    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @JsonIgnore
    @Column(name = "senha_hash", length = 100)
    private String senhaHash;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}