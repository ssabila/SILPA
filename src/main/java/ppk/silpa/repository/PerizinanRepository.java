package ppk.silpa.repository;

import ppk.silpa.entity.Perizinan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerizinanRepository extends JpaRepository<Perizinan, Long> {
    List<Perizinan> findByMahasiswaId(Long mahasiswaId);
}
