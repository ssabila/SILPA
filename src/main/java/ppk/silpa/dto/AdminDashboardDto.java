package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardDto {
    private long totalPengajuanSemuaWaktu;
    private Map<String, Long> jumlahPengajuanPerStatus;
    private Map<String, Long> jumlahPengajuanPerJenisIzin;
    private long pengajuanHariIni;
    private long pengajuanMingguIni;
    private long pengajuanBulanIni;
    private List<PerizinanDto> pengajuanPerluDiproses;
}
