package org.example.dao;

import org.example.model.PhongTro;
import java.sql.*;
import java.util.ArrayList;

public class PhongTroDAO {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongTro;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "123456";

    public static ArrayList<PhongTro> layDanhSachPhong() {
        ArrayList<PhongTro> danhSach = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM PhongTro")) {

            while (rs.next()) {
                String ma = rs.getString("MaPhong");
                String ten = rs.getString("TenPhong");
                double gia = rs.getDouble("GiaTien");
                String trangThai = rs.getString("TrangThai");
                danhSach.add(new PhongTro(ma, ten, gia, trangThai));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSach;
    }

    public static boolean themPhong(PhongTro pt) {
        String sql = "INSERT INTO PhongTro VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pt.getMaPhong());
            ps.setString(2, pt.getTenPhong());
            ps.setDouble(3, pt.getGiaThue());
            ps.setString(4, pt.getTrangThai());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean suaPhong(PhongTro pt) {
        String sql = "UPDATE PhongTro SET TenPhong = ?, GiaTien = ?, TrangThai = ? WHERE MaPhong = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pt.getTenPhong());
            ps.setDouble(2, pt.getGiaThue());
            ps.setString(3, pt.getTrangThai());
            ps.setString(4, pt.getMaPhong());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean xoaPhong(String maPhong) {
        String sql = "DELETE FROM PhongTro WHERE MaPhong = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}