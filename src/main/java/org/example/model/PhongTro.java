package org.example.model;

public class PhongTro {
    private String maPhong;
    private String tenPhong;
    private String loaiPhong;
    private double giaThueCoBan;
    private String trangThai; // "Trống", "Đang thuê", "Đang sửa"

    public PhongTro(String maPhong, String tenPhong, String loaiPhong, double giaThueCoBan, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.loaiPhong = loaiPhong;
        this.giaThueCoBan = giaThueCoBan;
        this.trangThai = trangThai;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { this.loaiPhong = loaiPhong; }
    public double getGiaThueCoBan() { return giaThueCoBan; }
    public void setGiaThueCoBan(double giaThueCoBan) { this.giaThueCoBan = giaThueCoBan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return maPhong + " - " + tenPhong;
    }
}