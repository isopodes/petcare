package petcare;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import petcare.model.Usuario;
import petcare.repository.UsuarioRepository;

// Os testes usam um banco em memória, separado do banco real (data/petcare)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:petcare_teste;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class PetCareApiTest {

    private static final String SENHA = "senha12345";

    @Autowired
    MockMvc mvc;

    @Autowired
    UsuarioRepository usuarios;

    // ================= Cadastro e login =================

    @Test
    void cadastroDevolve201SemExporSenha() throws Exception {
        String email = emailUnico();
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("Maria", email, SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void senhaEhGuardadaComoHash() throws Exception {
        String email = emailUnico();
        registrar("Maria", email, SENHA);

        Usuario u = usuarios.findByEmail(email).orElseThrow();
        assertTrue(u.getSenhaHash().startsWith("$2"), "deve ser um hash BCrypt");
        assertNotEquals(SENHA, u.getSenhaHash());
    }

    @Test
    void emailRepetidoDevolve409() throws Exception {
        String email = emailUnico();
        registrar("Maria", email, SENHA);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("Outra", email, SENHA)))
                .andExpect(status().isConflict());
    }

    @Test
    void senhaCurtaDevolve400() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais("Maria", emailUnico(), "123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginCorretoDevolveToken() throws Exception {
        String email = emailUnico();
        registrar("Maria", email, SENHA);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(null, email, SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.email").value(email));
    }

    @Test
    void senhaErradaDevolve401() throws Exception {
        String email = emailUnico();
        registrar("Maria", email, SENHA);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(null, email, "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    // ================= Rotas protegidas =================

    @Test
    void rotaProtegidaSemTokenDevolve401() throws Exception {
        mvc.perform(get("/api/animals"))
                .andExpect(status().isUnauthorized());
    }

    // ================= Animais e autorização =================

    @Test
    void animalSemNomeDevolve400() throws Exception {
        String token = novoUsuarioComToken();
        postar("/api/animals", token, "{\"nome\":\"\",\"especie\":\"Gato\"}", 400);
    }

    @Test
    void listaMostraSoOsAnimaisDoProprioUsuario() throws Exception {
        String tokenA = novoUsuarioComToken();
        String tokenB = novoUsuarioComToken();
        criarAnimal(tokenA, "Rex");

        mvc.perform(get("/api/animals").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Rex"));

        mvc.perform(get("/api/animals").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void usuarioNaoAcessaAnimalDeOutro() throws Exception {
        String tokenA = novoUsuarioComToken();
        String tokenB = novoUsuarioComToken();
        long animalDeA = criarAnimal(tokenA, "Rex");

        // o dono acessa
        consultar("/api/animals/" + animalDeA, tokenA, 200);
        consultar("/api/animals/" + animalDeA + "/history", tokenA, 200);

        // outro usuário recebe 404, como se o animal não existisse
        consultar("/api/animals/" + animalDeA, tokenB, 404);
        consultar("/api/animals/" + animalDeA + "/history", tokenB, 404);
        consultar("/api/animals/" + animalDeA + "/appointments", tokenB, 404);
        consultar("/api/animals/" + animalDeA + "/vaccines", tokenB, 404);
    }

    @Test
    void usuarioNaoApagaAnimalDeOutro() throws Exception {
        String tokenA = novoUsuarioComToken();
        String tokenB = novoUsuarioComToken();
        long animalDeA = criarAnimal(tokenA, "Rex");

        mvc.perform(delete("/api/animals/" + animalDeA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // o animal continua existindo para o dono
        consultar("/api/animals/" + animalDeA, tokenA, 200);
    }

    // ================= Consultas, vacinas e timeline =================

    @Test
    void usuarioNaoRegistraConsultaNoAnimalAlheio() throws Exception {
        String tokenA = novoUsuarioComToken();
        String tokenB = novoUsuarioComToken();
        long animalDeA = criarAnimal(tokenA, "Rex");

        postar("/api/animals/" + animalDeA + "/appointments", tokenB, CONSULTA, 404);
    }

    @Test
    void vacinaComProximaDataAnteriorDevolve400() throws Exception {
        String token = novoUsuarioComToken();
        long animal = criarAnimal(token, "Rex");

        String vacinaInvalida =
                "{\"nome\":\"V10\",\"dataAplicacao\":\"2026-01-20\",\"proximaData\":\"2026-01-10\"}";
        postar("/api/animals/" + animal + "/vaccines", token, vacinaInvalida, 400);
    }

    @Test
    void timelineVemOrdenadaEPodeSerFiltrada() throws Exception {
        String token = novoUsuarioComToken();
        long animal = criarAnimal(token, "Rex");

        postar("/api/animals/" + animal + "/vaccines", token, VACINA, 201);       // janeiro
        postar("/api/animals/" + animal + "/appointments", token, CONSULTA, 201); // março

        // da mais recente para a mais antiga: a consulta (março) vem antes da vacina (janeiro)
        mvc.perform(get("/api/animals/" + animal + "/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("CONSULTA"))
                .andExpect(jsonPath("$[1].tipo").value("VACINA"));

        // filtro por tipo
        mvc.perform(get("/api/animals/" + animal + "/history?tipo=VACINA")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("VACINA"));
    }

    // ================= Ajudantes (evitam repetir código) =================

    private static final String CONSULTA =
            "{\"profissional\":\"Dra. Ana\",\"clinica\":\"Clinica Vet\","
                    + "\"dataHora\":\"2026-03-15T14:30:00\",\"motivo\":\"Check-up anual\"}";

    private static final String VACINA =
            "{\"nome\":\"V10\",\"dataAplicacao\":\"2026-01-20\",\"proximaData\":\"2027-01-20\"}";

    private String emailUnico() {
        return "teste-" + UUID.randomUUID() + "@email.com";
    }

    private String credenciais(String nome, String email, String senha) {
        String parteNome = (nome == null) ? "" : "\"nome\":\"" + nome + "\",";
        return "{" + parteNome + "\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }

    private void registrar(String nome, String email, String senha) throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(nome, email, senha)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(null, email, senha)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    // Cria um usuário novo e devolve o token dele
    private String novoUsuarioComToken() throws Exception {
        String email = emailUnico();
        registrar("Teste", email, SENHA);
        return login(email, SENHA);
    }

    private long criarAnimal(String token, String nome) throws Exception {
        String corpo = mvc.perform(post("/api/animals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\",\"especie\":\"Cachorro\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(corpo, "$.id");
        return id.longValue();
    }

    private void postar(String url, String token, String corpo, int esperado) throws Exception {
        mvc.perform(post(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().is(esperado));
    }

    private void consultar(String url, String token, int esperado) throws Exception {
        mvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().is(esperado));
    }
}