package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistikPerBulanDto {
    private int tahun;
    private int bulan; // 1-12
    private String namaBulanTahun; // Misal: "Januari 2023"
    private long jumlahPengajuan;
}
