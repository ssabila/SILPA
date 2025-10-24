package ppk.silpa.dto;

import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class AjukanIzinDto {
    @NotNull(message = "Jenis izin tidak boleh kosong")
    private JenisIzin jenisIzin;

    @NotNull(message = "Detail izin tidak boleh kosong")
    private DetailIzin detailIzin;

    private String deskripsi;

    @Valid
    @NotEmpty(message = "Minimal harus ada 1 sesi yang diajukan")
    private List<DetailSesiIzinDto> daftarSesi;
}
