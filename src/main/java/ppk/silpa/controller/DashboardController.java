package ppk.silpa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppk.silpa.dto.AdminDashboardDto;
import ppk.silpa.dto.MahasiswaDashboardDto;
import ppk.silpa.service.PerizinanService;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final PerizinanService perizinanService;

    public DashboardController(PerizinanService perizinanService) {
        this.perizinanService = perizinanService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/dashboard")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        AdminDashboardDto dashboardData = perizinanService.getAdminDashboardData();
        return ResponseEntity.ok(dashboardData);
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @GetMapping("/mahasiswa/dashboard")
    public ResponseEntity<MahasiswaDashboardDto> getMahasiswaDashboard() {
        MahasiswaDashboardDto dashboardData = perizinanService.getMahasiswaDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}
