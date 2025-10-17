package ppk.silpa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PerbaruiProfilDto {
    @NotEmpty(message = "Nama lengkap tidak boleh kosong")
    private String namaLengkap;

    @NotEmpty(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;
}
