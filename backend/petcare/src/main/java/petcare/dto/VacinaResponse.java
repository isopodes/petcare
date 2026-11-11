package petcare.dto;

import java.time.LocalDate;
import petcare.model.Vacina;

public record VacinaResponse(
        Long id,
        Long animalId,
        String nome,
        LocalDate dataAplicacao,
        LocalDate proximaData,
        String observacoes
) {
    public static VacinaResponse from(Vacina v) {
        return new VacinaResponse(v.getId(), v.getAnimal().getId(), v.getNome(),
                v.getDataAplicacao(), v.getProximaData(), v.getObservacoes());
    }
}