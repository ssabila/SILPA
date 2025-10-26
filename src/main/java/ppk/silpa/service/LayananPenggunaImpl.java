package ppk.silpa.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppk.silpa.dto.*;
import ppk.silpa.dto.UpdateNamaDto;
import ppk.silpa.entity.Pengguna;
import ppk.silpa.entity.Perizinan;
import ppk.silpa.entity.Role;
import ppk.silpa.entity.StatusPengajuan;
import ppk.silpa.exception.ResourceNotFoundException;
import ppk.silpa.exception.SilpaAPIException;
import ppk.silpa.repository.PenggunaRepository;
import ppk.silpa.repository.PerizinanRepository;
import ppk.silpa.util.PerizinanMapper;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class LayananPenggunaImpl implements LayananPengguna {

    private final PenggunaRepository repositoriPengguna;
    private final PasswordEncoder penyandiBentuk;
    private final PerizinanRepository repositoriPerizinan;

    public LayananPenggunaImpl(PenggunaRepository repositoriPengguna,
                               PasswordEncoder penyandiBentuk,
                               PerizinanRepository repositoriPerizinan) {
        this.repositoriPengguna = repositoriPengguna;
        this.penyandiBentuk = penyandiBentuk;
        this.repositoriPerizinan = repositoriPerizinan;
    }

    @Override
    public ProfilPenggunaDto ambilProfil() {
        Pengguna pengguna = ambilPenggunaSekarang();
        return mapToProfilDto(pengguna);
    }

    @Override
    public ProfilPenggunaDto perbaruiProfil(PerbaruiProfilDto perbaruiProfilDto) {
        Pengguna pengguna = ambilPenggunaSekarang();
        if (!pengguna.getEmail().equals(perbaruiProfilDto.getEmail()) &&
                repositoriPengguna.existsByEmail(perbaruiProfilDto.getEmail())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Email sudah digunakan!");
        }
        pengguna.setNamaLengkap(perbaruiProfilDto.getNamaLengkap());
        pengguna.setEmail(perbaruiProfilDto.getEmail());
        Pengguna diperbarui = repositoriPengguna.save(pengguna);
        return mapToProfilDto(diperbarui);
    }

    @Override
    public void gantiKataSandi(GantiKataSandiDto gantiKataSandiDto) {
        Pengguna pengguna = ambilPenggunaSekarang();
        // Validasi kata sandi lama
        if (!penyandiBentuk.matches(gantiKataSandiDto.getKataSandiLama(), pengguna.getKataSandi())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Kata sandi lama tidak sesuai!");
        }
        // Validasi konfirmasi kata sandi baru
        if (!gantiKataSandiDto.getKataSandiBaru().equals(gantiKataSandiDto.getKonfirmasiKataSandi())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Kata sandi baru dan konfirmasi tidak sesuai!");
        }
        // Validasi kata sandi baru tidak sama dengan lama
        if (penyandiBentuk.matches(gantiKataSandiDto.getKataSandiBaru(), pengguna.getKataSandi())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Kata sandi baru tidak boleh sama dengan kata sandi lama!");
        }
        // Update kata sandi
        pengguna.setKataSandi(penyandiBentuk.encode(gantiKataSandiDto.getKataSandiBaru()));
        repositoriPengguna.save(pengguna);
    }

    @Override
    public void hapusAkun() {
        Pengguna pengguna = ambilPenggunaSekarang();
        repositoriPengguna.deleteById(pengguna.getId());
    }

    @Override
    public List<ProfilPenggunaDto> getAllMahasiswaForAdmin() {
        List<Pengguna> allUsers = repositoriPengguna.findAll();
        List<ProfilPenggunaDto> mahasiswaList = allUsers.stream()
                .filter(user -> user.getPeran() == Role.MAHASISWA)
                .map(this::mapToProfilDto)
                .sorted(Comparator.comparing(ProfilPenggunaDto::getNamaLengkap))
                .collect(Collectors.toList());

        return mahasiswaList;
    }

    @Override
    public MahasiswaDetailAdminDto getMahasiswaDetailForAdmin(Long mahasiswaId) {
        // Cari pengguna berdasarkan ID
        Pengguna mahasiswa = repositoriPengguna.findById(mahasiswaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna", "id", mahasiswaId));
        // Pastikan pengguna adalah mahasiswa
        if (mahasiswa.getPeran() != Role.MAHASISWA) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Pengguna dengan ID tersebut bukan mahasiswa.");
        }

        // Ambil semua perizinan mahasiswa tersebut
        List<Perizinan> perizinanList = repositoriPerizinan.findByMahasiswaId(mahasiswaId);

        // Hitung statistik
        long totalIzin = perizinanList.size();

        Map<String, Long> perStatus = perizinanList.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));
        Arrays.stream(StatusPengajuan.values()).forEach(s -> perStatus.putIfAbsent(s.name(), 0L));

        Map<String, Long> perJenisIzin = perizinanList.stream()
                .collect(Collectors.groupingBy(p -> p.getJenisIzin().name(), Collectors.counting()));
        Arrays.stream(ppk.silpa.entity.JenisIzin.values()).forEach(j -> perJenisIzin.putIfAbsent(j.name(), 0L));

        int totalBobot = perizinanList.stream()
                .filter(p -> p.getStatus() == StatusPengajuan.DISETUJUI)
                .mapToInt(Perizinan::getBobotKehadiran).sum();

        // Map daftar perizinan ke DTO
        List<PerizinanDto> semuaPerizinanDto = perizinanList.stream()
                .sorted(Comparator.comparing(Perizinan::getId).reversed())
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());

        // Gabungkan semua data ke DTO detail admin
        return new MahasiswaDetailAdminDto(
                mapToProfilDto(mahasiswa),
                totalIzin,
                perStatus,
                perJenisIzin,
                totalBobot,
                semuaPerizinanDto
        );
    }

    public ProfilPenggunaDto updateNama(UpdateNamaDto updateNamaDto) {
        Pengguna pengguna = ambilPenggunaSekarang();
        pengguna.setNamaLengkap(updateNamaDto.getNamaLengkap());
        Pengguna updatedPengguna = repositoriPengguna.save(pengguna);
        return mapToProfilDto(updatedPengguna);
    }

    @Override
    public ProfilPenggunaDto updateEmail(UpdateEmailDto updateEmailDto) {
        Pengguna pengguna = ambilPenggunaSekarang();
        // Cek jika email diubah dan email baru sudah ada
        if (!pengguna.getEmail().equals(updateEmailDto.getEmail()) &&
                repositoriPengguna.existsByEmail(updateEmailDto.getEmail())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Email sudah digunakan!");
        }
        pengguna.setEmail(updateEmailDto.getEmail());
        Pengguna updatedPengguna = repositoriPengguna.save(pengguna);
        return mapToProfilDto(updatedPengguna);
    }

    private Pengguna ambilPenggunaSekarang() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repositoriPengguna.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan dengan email: " + email));
    }

    private ProfilPenggunaDto mapToProfilDto(Pengguna pengguna) {
        return new ProfilPenggunaDto(
                pengguna.getId(),
                pengguna.getNamaLengkap(),
                pengguna.getEmail(),
                pengguna.getPeran().toString()
        );
    }
}

