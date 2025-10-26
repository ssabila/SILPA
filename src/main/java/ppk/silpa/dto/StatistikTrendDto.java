package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistikTrendDto {
    private long jumlahBulanIni;
    private long jumlahBulanLalu;
    private double persentasePerubahan; // Positif jika naik, negatif jika turun
    private String deskripsiPerubahan; // Misal: "Naik 10.5%" atau "Turun 5.2%"
}
