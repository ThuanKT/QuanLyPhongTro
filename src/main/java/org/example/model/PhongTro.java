package org.example.model;

public class PhongTro {
    private String maPhong;
    private String tenPhong;
    private double giaThue;
    private String trangThai;

    public PhongTro(String maPhong, String tenPhong, double giaThue, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.giaThue = giaThue;
        this.trangThai = trangThai;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public double getGiaThue() { return giaThue; }
    public void setGiaThue(double giaThue) { this.giaThue = giaThue; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}