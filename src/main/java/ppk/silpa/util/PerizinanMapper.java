package ppk.silpa.util;

import ppk.silpa.dto.BerkasDto;
import ppk.silpa.dto.DetailSesiIzinDto;
import ppk.silpa.dto.PerizinanDto;
import ppk.silpa.entity.Perizinan;

import java.util.stream.Collectors;
import java.util.List;

public class PerizinanMapper {

    public static PerizinanDto mapToPerizinanDto(Perizinan perizinan) {

        List<DetailSesiIzinDto> daftarSesiDto = perizinan.getDaftarSesiIzin().stream()
                .map(sesi -> {
                    DetailSesiIzinDto dto = new DetailSesiIzinDto();
                    dto.setTanggal(sesi.getTanggal());
                    dto.setNamaMataKuliah(sesi.getNamaMataKuliah());
                    dto.setNamaDosen(sesi.getNamaDosen());
                    dto.setSesi1(sesi.isSesi1());
                    dto.setSesi2(sesi.isSesi2());
                    dto.setSesi3(sesi.isSesi3());
                    return dto;
                }).collect(Collectors.toList());

        List<BerkasDto> daftarBerkasDto = perizinan.getDaftarBerkas().stream().map(berkas -> new BerkasDto(
                berkas.getId(),
                berkas.getNamaFile(),
                berkas.getUrlAksesFile()
        )).collect(Collectors.toList());

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
                daftarBerkasDto,
                daftarSesiDto
        );
    }
}

