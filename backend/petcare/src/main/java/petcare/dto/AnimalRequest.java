package petcare.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record AnimalRequest(
        @NotNull Long usuarioId,
        @NotBlank @Size(max = 100) String nome,
        @NotBlank @Size(max = 80) String especie,
        @Size(max = 100) String raca,
        @PastOrPresent LocalDate nascimentoEstimado,
        @Size(max = 20) String sexo,
        @Size(max = 2000) String observacoes
) {}