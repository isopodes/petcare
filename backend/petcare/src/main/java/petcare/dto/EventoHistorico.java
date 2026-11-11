package petcare.dto;

import java.time.LocalDateTime;

public record EventoHistorico(
        String tipo,
        String titulo,
        String descricao,
        LocalDateTime data
) {}