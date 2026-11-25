package org.example.model;

import java.sql.Date;

public class ChiSoDienNuoc {
    private String maChiSo;
    private String maPhong;
    private String thangNam;
    private double chiSoDienCu;
    private double chiSoDienMoi;
    private double chiSoNuocCu;
    private double chiSoNuocMoi;
    private Date ngayGhi;

    public ChiSoDienNuoc(String maChiSo, String maPhong, String thangNam, double chiSoDienCu, double chiSoDienMoi, double chiSoNuocCu, double chiSoNuocMoi, Date ngayGhi) {
        this.maChiSo = maChiSo;
        this.maPhong = maPhong;
        this.thangNam = thangNam;
        this.chiSoDienCu = chiSoDienCu;
        this.chiSoDienMoi = chiSoDienMoi;
        this.chiSoNuocCu = chiSoNuocCu;
        this.chiSoNuocMoi = chiSoNuocMoi;
        this.ngayGhi = ngayGhi;
    }

    public double tinhLuongDienTieuThu() { return chiSoDienMoi - chiSoDienCu; }
    public double tinhLuongNuocTieuThu() { return chiSoNuocMoi - chiSoNuocCu; }

    public String getMaChiSo() { return maChiSo; }
    public String getMaPhong() { return maPhong; }
    public String getThangNam() { return thangNam; }
    public double getChiSoDienCu() { return chiSoDienCu; }
    public double getChiSoDienMoi() { return chiSoDienMoi; }
    public double getChiSoNuocCu() { return chiSoNuocCu; }
    public double getChiSoNuocMoi() { return chiSoNuocMoi; }
    public Date getNgayGhi() { return ngayGhi; }
}