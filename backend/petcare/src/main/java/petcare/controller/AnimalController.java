package petcare.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
    public AnimalResponse criar(@Valid @RequestBody AnimalRequest req) {
        Usuario dono = usuarios.findById(req.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        Animal animal = new Animal();
        animal.setUsuario(dono);
        preencher(animal, req);
        return AnimalResponse.from(animais.save(animal));
    }

    @GetMapping
    public List<AnimalResponse> listar(@RequestParam(required = false) Long usuarioId) {
        List<Animal> lista = (usuarioId == null)
                ? animais.findAll()
                : animais.findByUsuarioId(usuarioId);
        return lista.stream().map(AnimalResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AnimalResponse buscar(@PathVariable Long id) {
        return AnimalResponse.from(buscarOu404(id));
    }

    @PutMapping("/{id}")
    public AnimalResponse atualizar(@PathVariable Long id,
                                    @Valid @RequestBody AnimalRequest req) {
        Animal animal = buscarOu404(id);
        preencher(animal, req);
        return AnimalResponse.from(animais.save(animal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        animais.delete(buscarOu404(id));
    }

    private Animal buscarOu404(Long id) {
        return animais.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Animal não encontrado"));
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