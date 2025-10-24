package ppk.silpa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppk.silpa.dto.ApiResponse;
import ppk.silpa.dto.InfoDetailIzinDto;
import ppk.silpa.dto.InfoJenisIzinDto;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/info-perizinan")
public class InfoPerizinanController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<InfoJenisIzinDto>>> getInfoPerizinan() {

        List<InfoJenisIzinDto> hasil = new ArrayList<>();

        // 1. Loop semua JenisIzin
        for (JenisIzin jenis : JenisIzin.values()) {
            InfoJenisIzinDto jenisDto = InfoJenisIzinDto.fromEntity(jenis);

            // 2. Cari detail yang sesuai
            List<InfoDetailIzinDto> detailList = EnumSet.allOf(DetailIzin.class).stream()
                    .filter(detail -> detail.getJenis() == jenis)
                    .map(InfoDetailIzinDto::fromEntity)
                    .collect(Collectors.toList());

            jenisDto.setDaftarDetail(detailList);
            hasil.add(jenisDto);
        }

        return ResponseEntity.ok(ApiResponse.sukses("Informasi perizinan berhasil diambil", hasil));
    }
}

