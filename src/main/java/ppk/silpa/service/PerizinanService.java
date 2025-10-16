package ppk.silpa.service;

import ppk.silpa.dto.AjukanIzinDto;
import ppk.silpa.dto.PerizinanDto;
import ppk.silpa.dto.UpdateStatusDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PerizinanService {
    PerizinanDto ajukanPerizinan(AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkas);
    List<PerizinanDto> getPerizinanByMahasiswa();
    PerizinanDto getPerizinanById(Long perizinanId);
    PerizinanDto perbaruiPerizinan(Long perizinanId, AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkas);
    void hapusPerizinan(Long perizinanId);

    // Metode untuk Admin
    List<PerizinanDto> getSemuaPerizinan();
    PerizinanDto perbaruiStatusPerizinan(Long perizinanId, UpdateStatusDto updateStatusDto);

}

