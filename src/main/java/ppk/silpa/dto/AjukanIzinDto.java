package ppk.silpa.dto;

import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class AjukanIzinDto {
    @NotNull(message = "Jenis izin tidak boleh kosong")
    private JenisIzin jenisIzin;

    @NotNull(message = "Detail izin tidak boleh kosong")
    private DetailIzin detailIzin;

    @NotNull(message = "Tanggal mulai tidak boleh kosong")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tanggalMulai;

    @NotNull(message = "Tanggal selesai tidak boleh kosong")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate tanggalSelesai;

    private String deskripsi;
}
