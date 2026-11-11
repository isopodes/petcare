package petcare.dto;

import java.time.LocalDateTime;
import petcare.model.Consulta;

public record ConsultaResponse(
        Long id,
        Long animalId,
        String profissional,
        String clinica,
        LocalDateTime dataHora,
        String motivo,
        String observacoes
) {
    public static ConsultaResponse from(Consulta c) {
        return new ConsultaResponse(c.getId(), c.getAnimal().getId(), c.getProfissional(),
                c.getClinica(), c.getDataHora(), c.getMotivo(), c.getObservacoes());
    }
}