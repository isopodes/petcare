package petcare.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record VacinaRequest(
        @NotBlank @Size(max = 100) String nome,
        @NotNull @PastOrPresent LocalDate dataAplicacao,
        LocalDate proximaData,
        @Size(max = 2000) String observacoes
) {}