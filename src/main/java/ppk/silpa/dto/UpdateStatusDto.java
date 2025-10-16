package ppk.silpa.dto;

import ppk.silpa.entity.StatusPengajuan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusDto {
    @NotNull(message = "Status tidak boleh kosong")
    private StatusPengajuan status;
    private String catatanAdmin;
}
