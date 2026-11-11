package petcare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import petcare.model.Consulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByAnimalId(Long animalId);
}