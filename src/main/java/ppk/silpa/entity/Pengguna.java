package ppk.silpa.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pengguna")
public class Pengguna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String namaLengkap;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String kataSandi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role peran;
}

