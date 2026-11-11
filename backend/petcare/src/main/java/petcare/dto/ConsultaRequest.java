package petcare.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ConsultaRequest(
        @Size(max = 150) String profissional,
        @Size(max = 150) String clinica,
        @NotNull LocalDateTime dataHora,
        @Size(max = 2000) String motivo,
        @Size(max = 2000) String observacoes
) {}