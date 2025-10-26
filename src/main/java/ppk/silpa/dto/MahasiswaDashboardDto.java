package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppk.silpa.entity.Perizinan;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MahasiswaDashboardDto {
    private long totalIzinDiajukan;
    private Map<String, Long> breakdownPerStatus;
    private Map<String, Long> breakdownPerJenisIzin;
    private int totalBobotTerpakai;
    private List<PerizinanDto> izinSedangDiproses;
    private List<PerizinanDto> riwayat5IzinTerakhir;
    private boolean adaPerluRevisi;
}
