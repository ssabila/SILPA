package ppk.silpa.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import ppk.silpa.entity.StatusPengajuan;
import ppk.silpa.exception.SilpaAPIException;
import ppk.silpa.repository.PerizinanRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Service
public class ValidasiPerizinanServiceImpl implements ValidasiPerizinanService {

    private final PerizinanRepository repositoriPerizinan;

    private static final int BATAS_HARI_PENGAJUAN = 7; // Maksimal 7 hari setelah kejadian

    public ValidasiPerizinanServiceImpl(PerizinanRepository repositoriPerizinan) {
        this.repositoriPerizinan = repositoriPerizinan;
    }

    @Override
    public void validasiTanggalPengajuan(JenisIzin jenisIzin, DetailIzin detailIzin, LocalDate tanggalMulai) {
        LocalDate hariIni = LocalDate.now();

        // Untuk SAKIT dan IZIN_ALASAN_PENTING: Maksimal 7 hari setelah kejadian
        if (jenisIzin == JenisIzin.SAKIT || jenisIzin == JenisIzin.IZIN_ALASAN_PENTING) {
            long hariSelisih = ChronoUnit.DAYS.between(tanggalMulai, hariIni);

            if (hariSelisih > BATAS_HARI_PENGAJUAN) {
                throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                        "Pengajuan " + jenisIzin + " hanya boleh dilakukan maksimal 7 hari setelah kejadian!");
            }

            if (hariSelisih < 0) {
                throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                        "Tanggal mulai tidak boleh di masa depan!");
            }
        }

        // Untuk DISPENSASI_INSTITUSI: Harus sebelum hari H
        if (jenisIzin == JenisIzin.DISPENSASI_INSTITUSI) {
            if (tanggalMulai.isBefore(hariIni)) {
                throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                        "Pengajuan dispensasi institusi harus dilakukan sebelum hari pelaksanaan!");
            }
        }
    }

    @Override
    public void validasiRangeTanggal(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        if (tanggalMulai.isAfter(tanggalSelesai)) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Tanggal mulai tidak boleh lebih besar dari tanggal selesai!");
        }

        if (tanggalMulai.isEqual(tanggalSelesai)) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Tanggal mulai dan tanggal selesai tidak boleh sama!");
        }
    }

    @Override
    public void validasiTidakAdaDuplikat(Long mahasiswaId, LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        boolean adaDuplikat = repositoriPerizinan.findByMahasiswaId(mahasiswaId)
                .stream()
                .filter(p -> p.getStatus() != StatusPengajuan.DITOLAK)
                .anyMatch(p ->
                        (tanggalMulai.isBefore(p.getTanggalSelesai()) || tanggalMulai.isEqual(p.getTanggalSelesai())) &&
                                (tanggalSelesai.isAfter(p.getTanggalMulai()) || tanggalSelesai.isEqual(p.getTanggalMulai()))
                );

        if (adaDuplikat) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Terdapat perizinan lain yang overlap dengan tanggal ini!");
        }
    }
}
