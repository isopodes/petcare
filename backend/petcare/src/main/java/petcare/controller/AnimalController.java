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
import petcare.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/animals")
public class AnimalController {

    private final AnimalRepository animais;
    private final UsuarioRepository usuarios;

    public AnimalController(AnimalRepository animais, UsuarioRepository usuarios) {
        this.animais = animais;
        this.usuarios = usuarios;
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

    // Lista só os animais do usuário logado
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
        animais.delete(buscarDoUsuario(id, jwt));
    }

    // O id do usuário vem do token (campo "subject"), nunca do corpo da requisição
    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    // Regra de autorização: o animal precisa pertencer ao usuário logado.
    // Se não pertencer, responde 404, igual a "não existe".
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