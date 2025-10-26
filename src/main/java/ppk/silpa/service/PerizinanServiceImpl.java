package ppk.silpa.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ppk.silpa.dto.*;
import ppk.silpa.exception.ResourceNotFoundException;
import ppk.silpa.exception.SilpaAPIException;
import ppk.silpa.entity.*;
import ppk.silpa.repository.BerkasRepository;
import ppk.silpa.repository.PenggunaRepository;
import ppk.silpa.repository.PerizinanRepository;
import ppk.silpa.util.PerizinanMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class PerizinanServiceImpl implements PerizinanService {

    private final PerizinanRepository perizinanRepository;
    private final PenggunaRepository penggunaRepository;
    private final BerkasRepository berkasRepository;
    private final FileStorageService fileStorageService;
    private final ValidasiPerizinanService validasiPerizinanService;

    public PerizinanServiceImpl(
            PerizinanRepository perizinanRepository,
            PenggunaRepository penggunaRepository,
            BerkasRepository berkasRepository,
            FileStorageService fileStorageService,
            ValidasiPerizinanService validasiPerizinanService) {
        this.perizinanRepository = perizinanRepository;
        this.penggunaRepository = penggunaRepository;
        this.berkasRepository = berkasRepository;
        this.fileStorageService = fileStorageService;
        this.validasiPerizinanService = validasiPerizinanService;
    }

    @Override
    public PerizinanDto ajukanPerizinan(AjukanIzinDto ajukanIzinDto, List<MultipartFile> daftarBerkas) {
        Pengguna mahasiswa = getCurrentUser();

        // Tentukan Tanggal Mulai dan Selesai dari daftar sesi
        LocalDate tanggalMulai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid atau kosong."));

        LocalDate tanggalSelesai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid atau kosong."));

        // Validasi
        validasiPerizinanService.validasiTanggalPengajuan(ajukanIzinDto.getJenisIzin(), ajukanIzinDto.getDetailIzin(), tanggalMulai);
        validasiPerizinanService.validasiTidakAdaDuplikat(mahasiswa.getId(), tanggalMulai, tanggalSelesai);

        // Buat entitas Perizinan
        Perizinan perizinan = new Perizinan();
        perizinan.setMahasiswa(mahasiswa);
        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(tanggalMulai);
        perizinan.setTanggalSelesai(tanggalSelesai);
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));

        // Buat entitas DetailSesiIzin
        for (DetailSesiIzinDto sesiDto : ajukanIzinDto.getDaftarSesi()) {
            DetailSesiIzin sesiEntity = new DetailSesiIzin();
            sesiEntity.setTanggal(sesiDto.getTanggal());
            sesiEntity.setNamaMataKuliah(sesiDto.getNamaMataKuliah());
            sesiEntity.setNamaDosen(sesiDto.getNamaDosen());
            sesiEntity.setSesi1(sesiDto.isSesi1());
            sesiEntity.setSesi2(sesiDto.isSesi2());
            sesiEntity.setSesi3(sesiDto.isSesi3());
            sesiEntity.setPerizinan(perizinan);
            perizinan.getDaftarSesiIzin().add(sesiEntity);
        }

        // Simpan Perizinan
        Perizinan perizinanTersimpan = perizinanRepository.save(perizinan);

        // Simpan Berkas
        if (daftarBerkas != null && !daftarBerkas.isEmpty()) {
            for (MultipartFile file : daftarBerkas) {
                if (file != null && !file.isEmpty()) {
                    String namaFile = fileStorageService.simpanFile(file, perizinanTersimpan.getId());
                    Berkas berkas = new Berkas();
                    berkas.setNamaFile(namaFile);
                    berkas.setUrlAksesFile("/files/" + perizinanTersimpan.getId() + "/" + namaFile);
                    berkas.setPerizinan(perizinanTersimpan);
                    berkasRepository.save(berkas);
                }
            }
        }

        return PerizinanMapper.mapToPerizinanDto(
                perizinanRepository.findById(perizinanTersimpan.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanTersimpan.getId())));
    }

    @Override
    public PerizinanDto perbaruiPerizinan(Long perizinanId, AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkasBaru) {
        Pengguna mahasiswa = getCurrentUser();
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak untuk mengubah perizinan ini.");
        }
        if (perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Hanya perizinan dengan status PERLU_REVISI yang dapat diubah.");
        }

        LocalDate tanggalMulai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid atau kosong."));
        LocalDate tanggalSelesai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid atau kosong."));

        // Validasi lagi setelah mendapatkan tanggal baru
        validasiPerizinanService.validasiTanggalPengajuan(ajukanIzinDto.getJenisIzin(), ajukanIzinDto.getDetailIzin(), tanggalMulai);


        // Update data perizinan
        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(tanggalMulai);
        perizinan.setTanggalSelesai(tanggalSelesai);
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setCatatanAdmin(null);

        // Hapus berkas lama dari DB dan list
        berkasRepository.deleteAll(perizinan.getDaftarBerkas());
        perizinan.getDaftarBerkas().clear();
        // Hapus sesi lama
        perizinan.getDaftarSesiIzin().clear();

        // Tambah berkas baru
        if (berkasBaru != null && !berkasBaru.isEmpty()) {
            for (MultipartFile file : berkasBaru) {
                if (file != null && !file.isEmpty()) {
                    String namaFile = fileStorageService.simpanFile(file, perizinan.getId());
                    Berkas berkas = new Berkas();
                    berkas.setNamaFile(namaFile);
                    berkas.setUrlAksesFile("/files/" + perizinan.getId() + "/" + namaFile);
                    berkas.setPerizinan(perizinan);
                    perizinan.getDaftarBerkas().add(berkas);
                }
            }
        }
        // Tambah Sesi baru
        for (DetailSesiIzinDto sesiDto : ajukanIzinDto.getDaftarSesi()) {
            DetailSesiIzin sesiEntity = new DetailSesiIzin();
            sesiEntity.setTanggal(sesiDto.getTanggal());
            sesiEntity.setNamaMataKuliah(sesiDto.getNamaMataKuliah());
            sesiEntity.setNamaDosen(sesiDto.getNamaDosen());
            sesiEntity.setSesi1(sesiDto.isSesi1());
            sesiEntity.setSesi2(sesiDto.isSesi2());
            sesiEntity.setSesi3(sesiDto.isSesi3());
            sesiEntity.setPerizinan(perizinan);
            perizinan.getDaftarSesiIzin().add(sesiEntity);
        }

        // Simpan perubahan
        Perizinan perizinanDiperbarui = perizinanRepository.save(perizinan);

        // Load ulang untuk memastikan berkas baru ter-load dalam DTO
        return PerizinanMapper.mapToPerizinanDto(
                perizinanRepository.findById(perizinanDiperbarui.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanDiperbarui.getId()))
        );
    }

    @Override
    public List<PerizinanDto> getPerizinanByMahasiswa() {
        Pengguna mahasiswa = getCurrentUser();
        List<Perizinan> daftarPerizinan = perizinanRepository.findByMahasiswaId(mahasiswa.getId());
        return daftarPerizinan.stream()
                .map(PerizinanMapper::mapToPerizinanDto)
                .sorted(Comparator.comparing(PerizinanDto::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void hapusPerizinan(Long perizinanId) {
        Pengguna mahasiswa = getCurrentUser();
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        // Validasi kepemilikan
        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak untuk menghapus perizinan ini.");
        }
        // Validasi status
        if (perizinan.getStatus() != StatusPengajuan.DIAJUKAN && perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Hanya perizinan yang belum diproses atau perlu revisi yang dapat dihapus.");
        }

        // Hapus perizinan
        perizinanRepository.delete(perizinan);
    }

    @Override
    public List<PerizinanDto> getSemuaPerizinan() {
        return perizinanRepository.findAll().stream()
                .map(PerizinanMapper::mapToPerizinanDto)
                .sorted(Comparator.comparing(PerizinanDto::getId).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public PerizinanDto getPerizinanById(Long perizinanId) {
        Pengguna pengguna = getCurrentUser();
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        // Validasi akses
        if (pengguna.getPeran() == Role.MAHASISWA && !perizinan.getMahasiswa().getId().equals(pengguna.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak akses untuk melihat detail perizinan ini.");
        }

        return PerizinanMapper.mapToPerizinanDto(perizinan);
    }

    @Override
    public PerizinanDto perbaruiStatusPerizinan(Long perizinanId, UpdateStatusDto updateStatusDto) {
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        // Hanya status DIAJUKAN atau PERLU_REVISI yang bisa diubah oleh admin melalui endpoint ini
        if (perizinan.getStatus() != StatusPengajuan.DIAJUKAN && perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Status hanya bisa diubah jika status saat ini DIAJUKAN atau PERLU_REVISI.");
        }

        // Validasi catatan wajib jika perlu revisi atau ditolak
        if ((updateStatusDto.getStatus() == StatusPengajuan.PERLU_REVISI || updateStatusDto.getStatus() == StatusPengajuan.DITOLAK)
                && (updateStatusDto.getCatatanAdmin() == null || updateStatusDto.getCatatanAdmin().isBlank())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Catatan admin (alasan) wajib diisi jika status diubah menjadi PERLU REVISI atau DITOLAK.");
        }

        // Jika status DISETUJUI, catatan opsional (set null jika blank)
        if (updateStatusDto.getStatus() == StatusPengajuan.DISETUJUI && updateStatusDto.getCatatanAdmin() != null && updateStatusDto.getCatatanAdmin().isBlank()) {
            perizinan.setCatatanAdmin(null);
        } else {
            perizinan.setCatatanAdmin(updateStatusDto.getCatatanAdmin());
        }

        perizinan.setStatus(updateStatusDto.getStatus());
        Perizinan perizinanDiperbarui = perizinanRepository.save(perizinan);
        return PerizinanMapper.mapToPerizinanDto(perizinanDiperbarui);
    }

    @Override
    public AdminDashboardDto getAdminDashboardData() {
        List<Perizinan> semuaPengajuan = perizinanRepository.findAll();
        LocalDate hariIni = LocalDate.now();
        LocalDate awalMingguIni = hariIni.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate akhirMingguIni = hariIni.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate awalBulanIni = hariIni.withDayOfMonth(1);
        LocalDate akhirBulanIni = hariIni.with(TemporalAdjusters.lastDayOfMonth());

        long totalPengajuan = semuaPengajuan.size();

        Map<String, Long> perStatus = semuaPengajuan.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));
        Arrays.stream(StatusPengajuan.values()).forEach(s -> perStatus.putIfAbsent(s.name(), 0L));

        Map<String, Long> perJenisIzin = semuaPengajuan.stream()
                .collect(Collectors.groupingBy(p -> p.getJenisIzin().name(), Collectors.counting()));
        Arrays.stream(JenisIzin.values()).forEach(j -> perJenisIzin.putIfAbsent(j.name(), 0L));

        long pengajuanHariIni = semuaPengajuan.stream()
                .filter(p -> p.getTanggalMulai() != null && p.getTanggalMulai().isEqual(hariIni))
                .count();
        long pengajuanMingguIni = semuaPengajuan.stream()
                .filter(p -> p.getTanggalMulai() != null && !p.getTanggalMulai().isBefore(awalMingguIni) && !p.getTanggalMulai().isAfter(akhirMingguIni))
                .count();
        long pengajuanBulanIni = semuaPengajuan.stream()
                .filter(p -> p.getTanggalMulai() != null && !p.getTanggalMulai().isBefore(awalBulanIni) && !p.getTanggalMulai().isAfter(akhirBulanIni))
                .count();

        List<PerizinanDto> perluDiproses = semuaPengajuan.stream()
                .filter(p -> p.getStatus() == StatusPengajuan.DIAJUKAN)
                .sorted(Comparator.comparing(Perizinan::getTanggalMulai))
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());

        return new AdminDashboardDto(
                totalPengajuan,
                perStatus,
                perJenisIzin,
                pengajuanHariIni,
                pengajuanMingguIni,
                pengajuanBulanIni,
                perluDiproses
        );
    }

    @Override
    public MahasiswaDashboardDto getMahasiswaDashboardData() {
        Pengguna mahasiswa = getCurrentUser();
        List<Perizinan> pengajuanMahasiswa = perizinanRepository.findByMahasiswaId(mahasiswa.getId());

        long totalIzin = pengajuanMahasiswa.size();

        Map<String, Long> perStatus = pengajuanMahasiswa.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));
        Arrays.stream(StatusPengajuan.values()).forEach(s -> perStatus.putIfAbsent(s.name(), 0L));

        Map<String, Long> perJenisIzin = pengajuanMahasiswa.stream()
                .collect(Collectors.groupingBy(p -> p.getJenisIzin().name(), Collectors.counting()));
        Arrays.stream(JenisIzin.values()).forEach(j -> perJenisIzin.putIfAbsent(j.name(), 0L));

        int totalBobot = pengajuanMahasiswa.stream()
                .filter(p -> p.getStatus() == StatusPengajuan.DISETUJUI)
                .mapToInt(Perizinan::getBobotKehadiran).sum();

        List<PerizinanDto> sedangDiproses = pengajuanMahasiswa.stream()
                .filter(p -> p.getStatus() == StatusPengajuan.DIAJUKAN || p.getStatus() == StatusPengajuan.PERLU_REVISI)
                .sorted(Comparator.comparing(Perizinan::getId).reversed())
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());

        List<PerizinanDto> riwayatTerakhir = pengajuanMahasiswa.stream()
                .sorted(Comparator.comparing(Perizinan::getId).reversed())
                .limit(5)
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());

        boolean adaRevisi = pengajuanMahasiswa.stream()
                .anyMatch(p -> p.getStatus() == StatusPengajuan.PERLU_REVISI);

        return new MahasiswaDashboardDto(
                totalIzin,
                perStatus,
                perJenisIzin,
                totalBobot,
                sedangDiproses,
                riwayatTerakhir,
                adaRevisi
        );
    }

    @Override
    public List<PerizinanDto> filterPerizinan(
            StatusPengajuan status,
            JenisIzin jenisIzin,
            DetailIzin detailIzin,
            LocalDate tanggalMulaiDari,
            LocalDate tanggalMulaiSampai,
            Long mahasiswaIdParam,
            String namaMahasiswa,
            Integer bulan,
            Integer tahun) {

        Pengguna penggunaSaatIni = getCurrentUser();
        List<Perizinan> perizinanList;

        // Tentukan list awal berdasarkan role dan parameter
        if (penggunaSaatIni.getPeran() == Role.ADMIN) {
            if (mahasiswaIdParam != null) {
                // Admin filter by ID
                perizinanList = perizinanRepository.findByMahasiswaId(mahasiswaIdParam);
            } else {
                // Admin lihat semua atau filter by nama
                perizinanList = perizinanRepository.findAll();
            }
        } else { // Jika Mahasiswa
            perizinanList = perizinanRepository.findByMahasiswaId(penggunaSaatIni.getId());
            mahasiswaIdParam = null;
            namaMahasiswa = null;
        }

        // Mulai filtering menggunakan Stream API
        Stream<Perizinan> stream = perizinanList.stream();

        if (status != null) stream = stream.filter(p -> p.getStatus() == status);
        if (jenisIzin != null) stream = stream.filter(p -> p.getJenisIzin() == jenisIzin);
        if (detailIzin != null) stream = stream.filter(p -> p.getDetailIzin() == detailIzin);
        if (tanggalMulaiDari != null) stream = stream.filter(p -> p.getTanggalMulai() != null && !p.getTanggalMulai().isBefore(tanggalMulaiDari));
        if (tanggalMulaiSampai != null) stream = stream.filter(p -> p.getTanggalMulai() != null && !p.getTanggalMulai().isAfter(tanggalMulaiSampai));

        // Filter nama hanya jika admin dan ID mahasiswa tidak dispesifikkan
        if (penggunaSaatIni.getPeran() == Role.ADMIN && mahasiswaIdParam == null && namaMahasiswa != null && !namaMahasiswa.isBlank()) {
            String lowerCaseNama = namaMahasiswa.toLowerCase();
            stream = stream.filter(p -> p.getMahasiswa().getNamaLengkap().toLowerCase().contains(lowerCaseNama));
        }

        // Filter bulan dan tahun
        if (bulan != null && tahun != null) {
            YearMonth ym = YearMonth.of(tahun, bulan);
            LocalDate awalBulan = ym.atDay(1);
            LocalDate akhirBulan = ym.atEndOfMonth();
            stream = stream.filter(p -> p.getTanggalMulai() != null && !p.getTanggalMulai().isBefore(awalBulan) && !p.getTanggalMulai().isAfter(akhirBulan));
        } else if (bulan != null) {
            stream = stream.filter(p -> p.getTanggalMulai() != null && p.getTanggalMulai().getMonthValue() == bulan);
        } else if (tahun != null) {
            stream = stream.filter(p -> p.getTanggalMulai() != null && p.getTanggalMulai().getYear() == tahun);
        }

        List<PerizinanDto> hasilFilter = stream
                .sorted(Comparator.comparing(Perizinan::getId).reversed())
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());

        return hasilFilter;
    }

    @Override
    public PerizinanDto updateDeskripsi(Long perizinanId, UpdateDeskripsiDto updateDeskripsiDto) {
        Pengguna mahasiswa = getCurrentUser();
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak untuk mengubah deskripsi perizinan ini.");
        }
        if (perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Deskripsi hanya dapat diubah saat status PERLU_REVISI.");
        }

        perizinan.setDeskripsi(updateDeskripsiDto.getDeskripsi());
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setCatatanAdmin(null);
        Perizinan updatedPerizinan = perizinanRepository.save(perizinan);
        return PerizinanMapper.mapToPerizinanDto(updatedPerizinan);
    }

    @Override
    public List<StatistikPerBulanDto> getStatistikPerBulan() {
        List<Perizinan> semuaPengajuan = perizinanRepository.findAll();
        Locale localeIndonesia = new Locale("id", "ID");

        // Kelompokkan berdasarkan Tahun-Bulan, urutkan descending (terbaru dulu)
        Map<YearMonth, Long> perBulan = semuaPengajuan.stream()
                .filter(p -> p.getTanggalMulai() != null)
                .collect(Collectors.groupingBy(
                        p -> YearMonth.from(p.getTanggalMulai()),
                        () -> new TreeMap<>(Comparator.<YearMonth>reverseOrder()),
                        Collectors.counting()
                ));

        // Ubah Map menjadi List DTO
        List<StatistikPerBulanDto> statistikList = perBulan.entrySet().stream()
                .map(entry -> {
                    YearMonth ym = entry.getKey();
                    String namaBulan = Month.of(ym.getMonthValue()).getDisplayName(TextStyle.FULL, localeIndonesia);
                    return new StatistikPerBulanDto(
                            ym.getYear(),
                            ym.getMonthValue(),
                            namaBulan + " " + ym.getYear(), // Format tampilan: "Oktober 2025"
                            entry.getValue()
                    );
                })
                .collect(Collectors.toList());

        return statistikList;
    }

    @Override
    public List<StatistikPerJenisDto> getStatistikPerJenisIzin() {
        List<Perizinan> semuaPengajuan = perizinanRepository.findAll();

        Map<JenisIzin, Long> perJenis = semuaPengajuan.stream()
                .collect(Collectors.groupingBy(Perizinan::getJenisIzin, Collectors.counting()));

        // Buat list DTO dari semua enum JenisIzin, isi jumlah dari map
        List<StatistikPerJenisDto> statistikList = Arrays.stream(JenisIzin.values())
                .map(jenis -> {
                    String namaTampilan = "";
                    switch (jenis) {
                        case SAKIT: namaTampilan = "Sakit"; break;
                        case DISPENSASI_INSTITUSI: namaTampilan = "Dispensasi Institusi"; break;
                        case IZIN_ALASAN_PENTING: namaTampilan = "Izin Alasan Penting"; break;
                    }
                    return new StatistikPerJenisDto(
                            jenis,
                            namaTampilan,
                            perJenis.getOrDefault(jenis, 0L)
                    );
                })
                .sorted(Comparator.comparingLong(StatistikPerJenisDto::getJumlahPengajuan).reversed())
                .collect(Collectors.toList());

        return statistikList;
    }

    @Override
    public StatistikTrendDto getStatistikTrend() {
        List<Perizinan> semuaPengajuan = perizinanRepository.findAll();
        YearMonth bulanIni = YearMonth.now();
        YearMonth bulanLalu = bulanIni.minusMonths(1);

        // Hitung jumlah pengajuan bulan ini
        long jumlahBulanIni = semuaPengajuan.stream()
                .filter(p -> p.getTanggalMulai() != null && YearMonth.from(p.getTanggalMulai()).equals(bulanIni))
                .count();

        // Hitung jumlah pengajuan bulan lalu
        long jumlahBulanLalu = semuaPengajuan.stream()
                .filter(p -> p.getTanggalMulai() != null && YearMonth.from(p.getTanggalMulai()).equals(bulanLalu))
                .count();

        double persentase = 0.0;
        String deskripsi = "Tidak ada perubahan"; // Default

        if (jumlahBulanLalu > 0) {
            // Hitung persentase jika bulan lalu ada pengajuan
            persentase = ((double) (jumlahBulanIni - jumlahBulanLalu) / jumlahBulanLalu) * 100.0;
            if (persentase > 0) {
                deskripsi = String.format(Locale.US, "Naik %.1f%%", persentase);
            } else if (persentase < 0) {
                deskripsi = String.format(Locale.US, "Turun %.1f%%", Math.abs(persentase));
            }
        } else if (jumlahBulanIni > 0) {
            persentase = Double.POSITIVE_INFINITY;
            deskripsi = "Naik dari 0";
        }

        return new StatistikTrendDto(jumlahBulanIni, jumlahBulanLalu, persentase, deskripsi);
    }


    private int hitungBobotKehadiran(JenisIzin jenisIzin, DetailIzin detailIzin) {

        if (jenisIzin == JenisIzin.SAKIT) {
            return detailIzin == DetailIzin.RAWAT_INAP ? 100 : 60;
        } else if (jenisIzin == JenisIzin.DISPENSASI_INSTITUSI) {
            return 100;
        } else {
            return 100;
        }
    }

    private Pengguna getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan dengan email: " + email));
    }
}

