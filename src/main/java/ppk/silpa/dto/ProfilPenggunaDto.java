package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfilPenggunaDto {
    private Long id;
    private String namaLengkap;
    private String email;
    private String peran;
}
