package org.example.model;

public class KhachThue {
    private String maKhach;
    private String hoTen;
    private String soCCCD;
    private String soDienThoai;
    private String queQuan;

    public KhachThue(String maKhach, String hoTen, String soCCCD, String soDienThoai, String queQuan) {
        this.maKhach = maKhach;
        this.hoTen = hoTen;
        this.soCCCD = soCCCD;
        this.soDienThoai = soDienThoai;
        this.queQuan = queQuan;
    }

    public String getMaKhach() { return maKhach; }
    public String getHoTen() { return hoTen; }
    public String getSoCCCD() { return soCCCD; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getQueQuan() { return queQuan; }

    @Override
    public String toString() {
        return maKhach + " - " + hoTen + " (" + soCCCD + ")";
    }
}