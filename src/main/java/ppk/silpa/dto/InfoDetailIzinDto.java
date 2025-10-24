package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ppk.silpa.entity.DetailIzin;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoDetailIzinDto {
    private String namaEnum;
    private String namaTampilan;
    private String deskripsi;
    private String syarat;

    public static InfoDetailIzinDto fromEntity(DetailIzin detailIzin) {
        return new InfoDetailIzinDto(
                detailIzin.name(),
                detailIzin.getNamaTampilan(),
                detailIzin.getDeskripsi(),
                detailIzin.getSyarat()
        );
    }
}

