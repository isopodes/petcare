package petcare.dto;

import java.time.LocalDate;
import petcare.model.Animal;

public record AnimalResponse(
        Long id,
        Long usuarioId,
        String nome,
        String especie,
        String raca,
        LocalDate nascimentoEstimado,
        String sexo,
        String observacoes
) {
    public static AnimalResponse from(Animal a) {
        return new AnimalResponse(a.getId(), a.getUsuario().getId(), a.getNome(),
                a.getEspecie(), a.getRaca(), a.getNascimentoEstimado(),
                a.getSexo(), a.getObservacoes());
    }
}