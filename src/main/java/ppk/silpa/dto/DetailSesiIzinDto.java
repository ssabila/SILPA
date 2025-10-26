package ppk.silpa.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DetailSesiIzinDto {

    @NotNull(message = "Tanggal sesi tidak boleh kosong")
    private LocalDate tanggal;

    @NotEmpty(message = "Nama mata kuliah tidak boleh kosong")
    private String namaMataKuliah;

    @NotEmpty(message = "Nama dosen tidak boleh kosong")
    private String namaDosen;

    private boolean sesi1;
    private boolean sesi2;
    private boolean sesi3;
}
