package ppk.silpa.service;

import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import java.time.LocalDate;

public interface ValidasiPerizinanService {
    void validasiTanggalPengajuan(JenisIzin jenisIzin, DetailIzin detailIzin, LocalDate tanggalMulai);
    void validasiRangeTanggal(LocalDate tanggalMulai, LocalDate tanggalSelesai);
    void validasiTidakAdaDuplikat(Long mahasiswaId, LocalDate tanggalMulai, LocalDate tanggalSelesai);
}