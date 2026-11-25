package org.example.model;

public class HoaDon {
    private String maHoaDon;
    private String maPhong;
    private String thangNam;
    private double tienPhong;
    private double tienDien;
    private double tienNuoc;
    private double phiDichVu;
    private double tongTien;
    private double daThanhToan;
    private String trangThaiThanhToan;

    public HoaDon(String maHoaDon, String maPhong, String thangNam, double tienPhong, double tienDien, double tienNuoc, double phiDichVu, double tongTien, double daThanhToan, String trangThaiThanhToan) {
        this.maHoaDon = maHoaDon;
        this.maPhong = maPhong;
        this.thangNam = thangNam;
        this.tienPhong = tienPhong;
        this.tienDien = tienDien;
        this.tienNuoc = tienNuoc;
        this.phiDichVu = phiDichVu;
        this.tongTien = tongTien;
        this.daThanhToan = daThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public String getMaPhong() { return maPhong; }
    public String getThangNam() { return thangNam; }
    public double getTienPhong() { return tienPhong; }
    public double getTienDien() { return tienDien; }
    public double getTienNuoc() { return tienNuoc; }
    public double getPhiDichVu() { return phiDichVu; }
    public double getTongTien() { return tongTien; }
    public double getDaThanhToan() { return daThanhToan; }
    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
}