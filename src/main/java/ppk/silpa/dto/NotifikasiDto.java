package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifikasiDto {
    private Long id;
    private String pesan;
    private LocalDateTime waktu;
    private boolean sudahDibaca;
    private String linkTerkait;
}
