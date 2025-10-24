package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppk.silpa.entity.JenisIzin;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoJenisIzinDto {
    private String namaEnum;
    private String namaTampilan;
    private List<InfoDetailIzinDto> daftarDetail;

    public static InfoJenisIzinDto fromEntity(JenisIzin jenisIzin) {
        String namaTampilan = "";
        switch (jenisIzin) {
            case SAKIT:
                namaTampilan = "Izin Sakit";
                break;
            case DISPENSASI_INSTITUSI:
                namaTampilan = "Dispensasi Institusi";
                break;
            case IZIN_ALASAN_PENTING:
                namaTampilan = "Izin Alasan Penting";
                break;
        }
        return new InfoJenisIzinDto(jenisIzin.name(), namaTampilan, null);
    }
}

