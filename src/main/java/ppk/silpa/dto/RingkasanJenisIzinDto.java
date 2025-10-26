package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppk.silpa.entity.JenisIzin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RingkasanJenisIzinDto {
    private JenisIzin jenisIzin;
    private long jumlah;
}
