package ppk.silpa.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GantiKataSandiDto {
    @NotEmpty(message = "Kata sandi lama tidak boleh kosong")
    private String kataSandiLama;

    @NotEmpty(message = "Kata sandi baru tidak boleh kosong")
    @Size(min = 8, message = "Kata sandi baru minimal 8 karakter")
    private String kataSandiBaru;

    @NotEmpty(message = "Konfirmasi kata sandi tidak boleh kosong")
    private String konfirmasiKataSandi;
}
