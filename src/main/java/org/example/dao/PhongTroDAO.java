package org.example.dao;

import org.example.model.PhongTro;
import org.example.model.TaiKhoan;
import java.sql.*;
import java.util.ArrayList;

public class PhongTroDAO {
    public static TaiKhoan kiemTraDangNhap(String user, String pass) {
        String sql = "SELECT * FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                TaiKhoan tk = new TaiKhoan(
                        rs.getString("TenDangNhap"),
                        rs.getString("MatKhau"),
                        rs.getString("VaiTro"),
                        rs.getString("HoTen")
                );
                tk.setMaPhong(rs.getString("MaPhong"));
                return tk;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<PhongTro> layDanhSachPhong() {
        ArrayList<PhongTro> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM PhongTro")) {
            while (rs.next()) {
                list.add(new PhongTro(
                        rs.getString("MaPhong"),
                        rs.getString("TenPhong"),
                        rs.getString("LoaiPhong"),
                        rs.getDouble("GiaThueCoBan"),
                        rs.getString("TrangThai")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean themPhong(PhongTro pt) {
        String sql = "INSERT INTO PhongTro VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pt.getMaPhong());
            ps.setString(2, pt.getTenPhong());
            ps.setString(3, pt.getLoaiPhong());
            ps.setDouble(4, pt.getGiaThueCoBan());
            ps.setString(5, pt.getTrangThai());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean suaPhong(PhongTro pt) {
        String sql = "UPDATE PhongTro SET TenPhong=?, LoaiPhong=?, GiaThueCoBan=?, TrangThai=? WHERE MaPhong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pt.getTenPhong());
            ps.setString(2, pt.getLoaiPhong());
            ps.setDouble(3, pt.getGiaThueCoBan());
            ps.setString(4, pt.getTrangThai());
            ps.setString(5, pt.getMaPhong());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean xoaPhong(String maPhong) {
        String sql = "DELETE FROM PhongTro WHERE MaPhong=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}