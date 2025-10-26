package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppk.silpa.entity.StatusPengajuan;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RingkasanStatusDto {
    private StatusPengajuan status;
    private long jumlah;
}
