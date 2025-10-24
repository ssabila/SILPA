package ppk.silpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "detail_sesi_izin")
public class DetailSesiIzin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relasi ke 'induk' perizinannya
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perizinan_id", nullable = false)
    private Perizinan perizinan;

    /**
     * Tanggal spesifik mata kuliah itu
     */
    @Column(nullable = false)
    private LocalDate tanggal;

    @Column(nullable = false)
    private String namaMataKuliah;

    @Column(nullable = false)
    private String namaDosen;

    /**
     * Sesi yang ditinggalkan pada tanggal tersebut
     */
    @Column(nullable = false)
    private boolean sesi1 = false;

    @Column(nullable = false)
    private boolean sesi2 = false;

    @Column(nullable = false)
    private boolean sesi3 = false;
}
