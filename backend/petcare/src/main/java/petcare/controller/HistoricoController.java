package petcare.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import petcare.dto.*;
import petcare.model.Animal;
import petcare.model.Consulta;
import petcare.model.Vacina;
import petcare.repository.AnimalRepository;
import petcare.repository.ConsultaRepository;
import petcare.repository.VacinaRepository;

@RestController
@RequestMapping("/api/animals/{animalId}")
public class HistoricoController {

    private final AnimalRepository animais;
    private final ConsultaRepository consultas;
    private final VacinaRepository vacinas;

    public HistoricoController(AnimalRepository animais,
                               ConsultaRepository consultas,
                               VacinaRepository vacinas) {
        this.animais = animais;
        this.consultas = consultas;
        this.vacinas = vacinas;
    }

    // ---------- Consultas ----------

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaResponse criarConsulta(@PathVariable Long animalId,
                                          @Valid @RequestBody ConsultaRequest req) {
        Consulta c = new Consulta();
        c.setAnimal(buscarAnimal(animalId));
        c.setProfissional(req.profissional());
        c.setClinica(req.clinica());
        c.setDataHora(req.dataHora());
        c.setMotivo(req.motivo());
        c.setObservacoes(req.observacoes());
        return ConsultaResponse.from(consultas.save(c));
    }

    @GetMapping("/appointments")
    public List<ConsultaResponse> listarConsultas(@PathVariable Long animalId) {
        buscarAnimal(animalId);
        return consultas.findByAnimalId(animalId).stream()
                .map(ConsultaResponse::from).toList();
    }

    // ---------- Vacinas ----------

    @PostMapping("/vaccines")
    @ResponseStatus(HttpStatus.CREATED)
    public VacinaResponse criarVacina(@PathVariable Long animalId,
                                      @Valid @RequestBody VacinaRequest req) {
        if (req.proximaData() != null && !req.proximaData().isAfter(req.dataAplicacao())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A próxima data deve ser posterior à data de aplicação");
        }
        Vacina v = new Vacina();
        v.setAnimal(buscarAnimal(animalId));
        v.setNome(req.nome());
        v.setDataAplicacao(req.dataAplicacao());
        v.setProximaData(req.proximaData());
        v.setObservacoes(req.observacoes());
        return VacinaResponse.from(vacinas.save(v));
    }

    @GetMapping("/vaccines")
    public List<VacinaResponse> listarVacinas(@PathVariable Long animalId) {
        buscarAnimal(animalId);
        return vacinas.findByAnimalId(animalId).stream()
                .map(VacinaResponse::from).toList();
    }

    // ---------- Timeline ----------

    @GetMapping("/history")
    public List<EventoHistorico> historico(@PathVariable Long animalId,
                                           @RequestParam(required = false) String tipo) {
        buscarAnimal(animalId);
        List<EventoHistorico> eventos = new ArrayList<>();

        for (Consulta c : consultas.findByAnimalId(animalId)) {
            String titulo = "Consulta" + (c.getClinica() != null ? " - " + c.getClinica() : "");
            eventos.add(new EventoHistorico("CONSULTA", titulo, c.getMotivo(), c.getDataHora()));
        }
        for (Vacina v : vacinas.findByAnimalId(animalId)) {
            eventos.add(new EventoHistorico("VACINA", "Vacina: " + v.getNome(),
                    v.getObservacoes(), v.getDataAplicacao().atStartOfDay()));
        }

        return eventos.stream()
                .filter(e -> tipo == null || e.tipo().equalsIgnoreCase(tipo))
                .sorted(Comparator.comparing(EventoHistorico::data).reversed())
                .toList();
    }

    private Animal buscarAnimal(Long id) {
        return animais.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Animal não encontrado"));
    }
}