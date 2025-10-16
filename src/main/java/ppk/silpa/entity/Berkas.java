package ppk.silpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "berkas")
public class Berkas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String namaFile;
    private String urlAksesFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perizinan_id", nullable = false)
    private Perizinan perizinan;
}

