package org.example.model;

public class TaiKhoan {
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private String hoTen;
    private String maPhong; // Thêm mã phòng cho tài khoản khách thuê

    public TaiKhoan(String tenDangNhap, String matKhau, String vaiTro, String hoTen) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.hoTen = hoTen;
    }

    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public String getVaiTro() { return vaiTro; }
    public String getHoTen() { return hoTen; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
}