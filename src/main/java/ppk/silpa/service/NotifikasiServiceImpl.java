package ppk.silpa.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ppk.silpa.dto.NotifikasiDto;
import ppk.silpa.entity.Pengguna;
import ppk.silpa.entity.Perizinan;
import ppk.silpa.entity.Role;
import ppk.silpa.entity.StatusPengajuan;
import ppk.silpa.repository.PenggunaRepository;
import ppk.silpa.repository.PerizinanRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotifikasiServiceImpl implements NotifikasiService {

    private final PerizinanRepository perizinanRepository;
    private final PenggunaRepository penggunaRepository;

    public NotifikasiServiceImpl(PerizinanRepository perizinanRepository, PenggunaRepository penggunaRepository) {
        this.perizinanRepository = perizinanRepository;
        this.penggunaRepository = penggunaRepository;
    }

    @Override
    public List<NotifikasiDto> getNotifikasiSaya() {
        Pengguna pengguna = getCurrentUser();
        List<NotifikasiDto> notifikasiList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (pengguna.getPeran() == Role.MAHASISWA) {
            List<Perizinan> perizinanMahasiswa = perizinanRepository.findByMahasiswaId(pengguna.getId());
            for (Perizinan p : perizinanMahasiswa) {
                if (p.getStatus() == StatusPengajuan.PERLU_REVISI) {
                    notifikasiList.add(new NotifikasiDto(
                            p.getId(),
                            "Perizinan ID " + p.getId() + " perlu direvisi. Catatan: " + (p.getCatatanAdmin() != null ? p.getCatatanAdmin() : "-"),
                            now,
                            false,
                            "/api/perizinan/" + p.getId()
                    ));
                } else if (p.getStatus() == StatusPengajuan.DISETUJUI) {
                    notifikasiList.add(new NotifikasiDto(
                            p.getId(),
                            "Perizinan ID " + p.getId() + " (" + p.getJenisIzin() + ") telah disetujui.",
                            now,
                            false,
                            "/api/perizinan/" + p.getId()
                    ));
                } else if (p.getStatus() == StatusPengajuan.DITOLAK) {
                    notifikasiList.add(new NotifikasiDto(
                            p.getId(),
                            "Perizinan ID " + p.getId() + " ditolak. Alasan: " + (p.getCatatanAdmin() != null ? p.getCatatanAdmin() : "-"),
                            now,
                            false,
                            "/api/perizinan/" + p.getId()
                    ));
                }
            }
        } else if (pengguna.getPeran() == Role.ADMIN) {
            List<Perizinan> perluDiproses = perizinanRepository.findAll().stream()
                    .filter(p -> p.getStatus() == StatusPengajuan.DIAJUKAN)
                    .collect(Collectors.toList());
            if (!perluDiproses.isEmpty()) {
                notifikasiList.add(new NotifikasiDto(
                        null,
                        "Terdapat " + perluDiproses.size() + " pengajuan izin baru yang perlu diproses.",
                        now,
                        false,
                        "/api/perizinan?status=DIAJUKAN"
                ));
            }
        }

        notifikasiList.sort(Comparator.comparing(NotifikasiDto::getWaktu, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(NotifikasiDto::getId, Comparator.nullsLast(Comparator.reverseOrder())));

        return notifikasiList;
    }

    private Pengguna getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan"));
    }
}
