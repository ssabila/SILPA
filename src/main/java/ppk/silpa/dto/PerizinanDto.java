package ppk.silpa.dto;

import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import ppk.silpa.entity.StatusPengajuan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerizinanDto {
    private Long id;
    private Long mahasiswaId;
    private String mahasiswaNama;
    private JenisIzin jenisIzin;
    private DetailIzin detailIzin;
    private LocalDate tanggalMulai;
    private LocalDate tanggalSelesai;
    private String deskripsi;
    private int bobotKehadiran;
    private StatusPengajuan status;
    private String catatanAdmin;
    private List<BerkasDto> daftarBerkas;
}
