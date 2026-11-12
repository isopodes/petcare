package petcare.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import petcare.dto.LoginRequest;
import petcare.dto.RegisterRequest;
import petcare.dto.TokenResponse;
import petcare.dto.UsuarioResponse;
import petcare.model.Usuario;
import petcare.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder,
                          JwtEncoder jwtEncoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(req.senha()));
        return UsuarioResponse.from(usuarios.save(u));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(this::credenciaisInvalidas);
        if (u.getSenhaHash() == null || !encoder.matches(req.senha(), u.getSenhaHash())) {
            throw credenciaisInvalidas();
        }
        return new TokenResponse(gerarToken(u), UsuarioResponse.from(u));
    }

    // O token guarda o id do usuário e vale por 2 horas.
    private String gerarToken(Usuario u) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(u.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(2, ChronoUnit.HOURS))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private ResponseStatusException credenciaisInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }
}