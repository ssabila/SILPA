package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MahasiswaDetailAdminDto {
    private ProfilPenggunaDto profil;
    private long totalIzinDiajukan;
    private Map<String, Long> breakdownPerStatus;
    private Map<String, Long> breakdownPerJenisIzin;
    private int totalBobotTerpakai;
    private List<PerizinanDto> daftarSemuaPerizinan;
}
