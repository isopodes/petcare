package petcare.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import petcare.model.Usuario;
import petcare.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final UsuarioRepository usuarios;

    public UsuarioController(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario criar(@Valid @RequestBody Usuario usuario) {
        if (usuarios.existsByEmail(usuario.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        return usuarios.save(usuario);
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarios.findAll();
    }
}