package petcare.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import petcare.model.Animal;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByUsuarioId(Long usuarioId);
}