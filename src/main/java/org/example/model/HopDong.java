package org.example.model;

import java.sql.Date;

public class HopDong {
    private String maHopDong;
    private String maPhong;
    private String maKhach;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private double tienCoc;
    private String trangThai; // "Hiệu lực", "Hết hạn", "Đã hủy"

    public HopDong(String maHopDong, String maPhong, String maKhach, Date ngayBatDau, Date ngayKetThuc, double tienCoc, String trangThai) {
        this.maHopDong = maHopDong;
        this.maPhong = maPhong;
        this.maKhach = maKhach;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tienCoc = tienCoc;
        this.trangThai = trangThai;
    }

    public String getMaHopDong() { return maHopDong; }
    public String getMaPhong() { return maPhong; }
    public String getMaKhach() { return maKhach; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }
    public double getTienCoc() { return tienCoc; }
    public String getTrangThai() { return trangThai; }
}