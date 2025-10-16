package ppk.silpa.dto;

import ppk.silpa.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDto {
    @NotEmpty(message = "Nama lengkap tidak boleh kosong")
    private String namaLengkap;

    @NotEmpty(message = "Email tidak boleh kosong")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotEmpty(message = "Kata sandi tidak boleh kosong")
    @Size(min = 6, message = "Kata sandi minimal 6 karakter")
    private String kataSandi;

    @NotNull(message = "Peran tidak boleh kosong")
    private Role peran;
}
