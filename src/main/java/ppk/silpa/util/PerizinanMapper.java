package ppk.silpa.util;

import ppk.silpa.dto.BerkasDto;
import ppk.silpa.dto.PerizinanDto;
import ppk.silpa.entity.Perizinan;

import java.util.stream.Collectors;

public class PerizinanMapper {

    public static PerizinanDto mapToPerizinanDto(Perizinan perizinan) {
        return new PerizinanDto(
                perizinan.getId(),
                perizinan.getMahasiswa().getId(),
                perizinan.getMahasiswa().getNamaLengkap(),
                perizinan.getJenisIzin(),
                perizinan.getDetailIzin(),
                perizinan.getTanggalMulai(),
                perizinan.getTanggalSelesai(),
                perizinan.getDeskripsi(),
                perizinan.getBobotKehadiran(),
                perizinan.getStatus(),
                perizinan.getCatatanAdmin(),
                perizinan.getDaftarBerkas().stream().map(berkas -> new BerkasDto(
                        berkas.getId(),
                        berkas.getNamaFile(),
                        berkas.getUrlAksesFile()
                )).collect(Collectors.toList())
        );
    }
}

