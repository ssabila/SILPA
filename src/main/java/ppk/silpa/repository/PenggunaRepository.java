package ppk.silpa.repository;

import ppk.silpa.entity.Pengguna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PenggunaRepository extends JpaRepository<Pengguna, Long> {
    Optional<Pengguna> findByEmail(String email);
    Boolean existsByEmail(String email);
}

