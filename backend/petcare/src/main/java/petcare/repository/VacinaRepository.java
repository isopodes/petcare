package petcare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import petcare.model.Vacina;

public interface VacinaRepository extends JpaRepository<Vacina, Long> {
    List<Vacina> findByAnimalId(Long animalId);
}