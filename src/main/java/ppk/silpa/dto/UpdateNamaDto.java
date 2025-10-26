package ppk.silpa.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateNamaDto {
    @NotEmpty(message = "Nama lengkap tidak boleh kosong")
    private String namaLengkap;
}
