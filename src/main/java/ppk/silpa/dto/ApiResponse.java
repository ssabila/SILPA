package ppk.silpa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean berhasil;
    private String pesan;
    private T data;
    private LocalDateTime waktu;

    public static <T> ApiResponse<T> sukses(String pesan, T data) {
        return ApiResponse.<T>builder()
                .berhasil(true)
                .pesan(pesan)
                .data(data)
                .waktu(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> gagal(String pesan) {
        return ApiResponse.<T>builder()
                .berhasil(false)
                .pesan(pesan)
                .waktu(LocalDateTime.now())
                .build();
    }
}