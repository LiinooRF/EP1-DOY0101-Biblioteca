package cl.triskeledu.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.triskeledu.catalogo.model.Libro;
import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    Optional<Libro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // Spring Data arma la consulta solo a partir del nombre del metodo
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
}