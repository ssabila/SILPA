package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppk.silpa.entity.JenisIzin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistikPerJenisDto {
    private JenisIzin jenisIzin;
    private String namaJenisIzin; // Nama tampilan
    private long jumlahPengajuan;
}
