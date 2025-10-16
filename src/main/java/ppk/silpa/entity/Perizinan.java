package ppk.silpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perizinan")
public class Perizinan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mahasiswa_id", nullable = false)
    private Pengguna mahasiswa;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private JenisIzin jenisIzin;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private DetailIzin detailIzin;

    @Column(nullable = false)
    private LocalDate tanggalMulai;

    @Column(nullable = false)
    private LocalDate tanggalSelesai;

    @Lob // Untuk teks yang lebih panjang
    private String deskripsi;

    private int bobotKehadiran;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusPengajuan status;

    @Lob
    private String catatanAdmin;

    @OneToMany(mappedBy = "perizinan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Berkas> daftarBerkas = new ArrayList<>();
}
