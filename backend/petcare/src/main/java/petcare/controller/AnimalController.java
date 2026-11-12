package petcare.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import petcare.dto.AnimalRequest;
import petcare.dto.AnimalResponse;
import petcare.model.Animal;
import petcare.model.Usuario;
import petcare.repository.AnimalRepository;
import petcare.repository.ConsultaRepository;
import petcare.repository.UsuarioRepository;
import petcare.repository.VacinaRepository;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private final AnimalRepository animais;
    private final UsuarioRepository usuarios;
    private final ConsultaRepository consultas;
    private final VacinaRepository vacinas;

    public AnimalController(AnimalRepository animais, UsuarioRepository usuarios,
                            ConsultaRepository consultas, VacinaRepository vacinas) {
        this.animais = animais;
        this.usuarios = usuarios;
        this.consultas = consultas;
        this.vacinas = vacinas;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnimalResponse criar(@AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody AnimalRequest req) {
        Usuario dono = usuarios.findById(usuarioId(jwt))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Usuário inválido"));
        Animal animal = new Animal();
        animal.setUsuario(dono);
        preencher(animal, req);
        return AnimalResponse.from(animais.save(animal));
    }

    @GetMapping
    public List<AnimalResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return animais.findByUsuarioId(usuarioId(jwt)).stream()
                .map(AnimalResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AnimalResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AnimalResponse.from(buscarDoUsuario(id, jwt));
    }

    @PutMapping("/{id}")
    public AnimalResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                    @PathVariable Long id,
                                    @Valid @RequestBody AnimalRequest req) {
        Animal animal = buscarDoUsuario(id, jwt);
        preencher(animal, req);
        return AnimalResponse.from(animais.save(animal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Animal animal = buscarDoUsuario(id, jwt);

        // Apaga o histórico vinculado antes de excluir o animal para evitar erro no banco
        consultas.deleteByAnimalId(id);
        vacinas.deleteByAnimalId(id);

        animais.delete(animal);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private Animal buscarDoUsuario(Long id, Jwt jwt) {
        Animal animal = animais.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Animal não encontrado"));
        if (!animal.getUsuario().getId().equals(usuarioId(jwt))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Animal não encontrado");
        }
        return animal;
    }

    private void preencher(Animal a, AnimalRequest r) {
        a.setNome(r.nome());
        a.setEspecie(r.especie());
        a.setRaca(r.raca());
        a.setNascimentoEstimado(r.nascimentoEstimado());
        a.setSexo(r.sexo());
        a.setObservacoes(r.observacoes());
    }
}