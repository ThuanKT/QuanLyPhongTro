package org.example.dao;

import org.example.model.*;
import java.sql.*;
import java.util.ArrayList;

public class NghiepVuDAO {
    // --- KHÁCH THUÊ & HỢP ĐỒNG ---
    public static ArrayList<HopDong> layDanhSachHopDong() {
        ArrayList<HopDong> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM HopDong")) {
            while (rs.next()) {
                list.add(new HopDong(
                        rs.getString("MaHopDong"), rs.getString("MaPhong"),
                        rs.getString("MaKhach"), rs.getDate("NgayBatDau"),
                        rs.getDate("NgayKetThuc"), rs.getDouble("TienCoc"),
                        rs.getString("TrangThai")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Luồng biểu đồ trình tự 2.5.4: Lập hợp đồng mới (Lưu khách -> Lưu HĐ -> Đổi phòng sang Đang thuê)
    public static boolean lapHopDongMoi(KhachThue kt, HopDong hd) {
        String sqlCheck = "SELECT TrangThai FROM PhongTro WHERE MaPhong = ?";
        String sqlKhach = "IF NOT EXISTS (SELECT 1 FROM KhachThue WHERE MaKhach = ?) " +
                "INSERT INTO KhachThue VALUES (?, ?, ?, ?, ?)";
        String sqlHD = "INSERT INTO HopDong VALUES (?, ?, ?, ?, ?, ?, N'Hiệu lực')";
        String sqlUpdatePhong = "UPDATE PhongTro SET TrangThai = N'Đang thuê' WHERE MaPhong = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, hd.getMaPhong());
                ResultSet rs = psCheck.executeQuery();
                if (!rs.next() || !rs.getString("TrangThai").equals("Trống")) {
                    return false; // Phòng không khả dụng
                }
            }

            try (PreparedStatement psK = conn.prepareStatement(sqlKhach);
                 PreparedStatement psH = conn.prepareStatement(sqlHD);
                 PreparedStatement psP = conn.prepareStatement(sqlUpdatePhong)) {

                psK.setString(1, kt.getMaKhach());
                psK.setString(2, kt.getMaKhach());
                psK.setString(3, kt.getHoTen());
                psK.setString(4, kt.getSoCCCD());
                psK.setString(5, kt.getSoDienThoai());
                psK.setString(6, kt.getQueQuan());
                psK.executeUpdate();

                psH.setString(1, hd.getMaHopDong());
                psH.setString(2, hd.getMaPhong());
                psH.setString(3, hd.getMaKhach());
                psH.setDate(4, hd.getNgayBatDau());
                psH.setDate(5, hd.getNgayKetThuc());
                psH.setDouble(6, hd.getTienCoc());
                psH.executeUpdate();

                psP.setString(1, hd.getMaPhong());
                psP.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean thanhLyHopDong(String maHD, String maPhong) {
        String sql1 = "UPDATE HopDong SET TrangThai = N'Đã hủy' WHERE MaHopDong = ?";
        String sql2 = "UPDATE PhongTro SET TrangThai = N'Trống' WHERE MaPhong = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sql1);
                 PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps1.setString(1, maHD);
                ps1.executeUpdate();
                ps2.setString(1, maPhong);
                ps2.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }
    public static boolean suaHopDong(String maHD, Date ngayBD, Date ngayKT, double tienCoc, String trangThai) {
        String sql = "UPDATE HopDong SET NgayBatDau = ?, NgayKetThuc = ?, TienCoc = ?, TrangThai = ? WHERE MaHopDong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, ngayBD);
            ps.setDate(2, ngayKT);
            ps.setDouble(3, tienCoc);
            ps.setString(4, trangThai);
            ps.setString(5, maHD);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- CHỈ SỐ ĐIỆN NƯỚC (IoT & Nhập tay - Mục 2.5.1) ---
    public static ArrayList<ChiSoDienNuoc> layDanhSachChiSo() {
        ArrayList<ChiSoDienNuoc> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ChiSoDienNuoc")) {
            while (rs.next()) {
                list.add(new ChiSoDienNuoc(
                        rs.getString("MaChiSo"), rs.getString("MaPhong"),
                        rs.getString("ThangNam"), rs.getDouble("ChiSoDienCu"),
                        rs.getDouble("ChiSoDienMoi"), rs.getDouble("ChiSoNuocCu"),
                        rs.getDouble("ChiSoNuocMoi"), rs.getDate("NgayGhi")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static boolean luuChiSoDienNuoc(ChiSoDienNuoc cs) {
        String sql = "INSERT INTO ChiSoDienNuoc VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cs.getMaChiSo());
            ps.setString(2, cs.getMaPhong());
            ps.setString(3, cs.getThangNam());
            ps.setDouble(4, cs.getChiSoDienCu());
            ps.setDouble(5, cs.getChiSoDienMoi());
            ps.setDouble(6, cs.getChiSoNuocCu());
            ps.setDouble(7, cs.getChiSoNuocMoi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // --- HÓA ĐƠN & THANH TOÁN (Mục 2.5.2 & 2.5.3) ---
    public static ArrayList<HoaDon> layDanhSachHoaDon() {
        ArrayList<HoaDon> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM HoaDon")) {
            while (rs.next()) {
                list.add(new HoaDon(
                        rs.getString("MaHoaDon"), rs.getString("MaPhong"),
                        rs.getString("ThangNam"), rs.getDouble("TienPhong"),
                        rs.getDouble("TienDien"), rs.getDouble("TienNuoc"),
                        rs.getDouble("PhiDichVu"), rs.getDouble("TongTien"),
                        rs.getDouble("DaThanhToan"), rs.getString("TrangThaiThanhToan")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static String tuDongLapHoaDon(String maHD, String maPhong, String thangNam, double donGiaDien, double donGiaNuoc, double phiDichVu) {
        String sqlPhong = "SELECT GiaThueCoBan, TrangThai FROM PhongTro WHERE MaPhong = ?";
        String sqlCS = "SELECT * FROM ChiSoDienNuoc WHERE MaPhong = ? AND ThangNam = ?";
        String sqlInsert = "INSERT INTO HoaDon VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, N'Chưa thanh toán')";

        try (Connection conn = DatabaseConnection.getConnection()) {
            double giaPhong = 0;
            try (PreparedStatement psP = conn.prepareStatement(sqlPhong)) {
                psP.setString(1, maPhong);
                ResultSet rsP = psP.executeQuery();
                if (!rsP.next()) return "Không tìm thấy phòng!";
                if (!rsP.getString("TrangThai").equals("Đang thuê")) return "Phòng hiện chưa có khách thuê!";
                giaPhong = rsP.getDouble("GiaThueCoBan");
            }

            double dienTieuThu = 0, nuocTieuThu = 0;
            try (PreparedStatement psC = conn.prepareStatement(sqlCS)) {
                psC.setString(1, maPhong);
                psC.setString(2, thangNam);
                ResultSet rsC = psC.executeQuery();
                if (!rsC.next()) return "Thiếu chỉ số điện/nước tháng " + thangNam + " của phòng này!";
                dienTieuThu = rsC.getDouble("ChiSoDienMoi") - rsC.getDouble("ChiSoDienCu");
                nuocTieuThu = rsC.getDouble("ChiSoNuocMoi") - rsC.getDouble("ChiSoNuocCu");
            }

            double tienDien = dienTieuThu * donGiaDien;
            double tienNuoc = nuocTieuThu * donGiaNuoc;
            double tongTien = giaPhong + tienDien + tienNuoc + phiDichVu;

            try (PreparedStatement psI = conn.prepareStatement(sqlInsert)) {
                psI.setString(1, maHD);
                psI.setString(2, maPhong);
                psI.setString(3, thangNam);
                psI.setDouble(4, giaPhong);
                psI.setDouble(5, tienDien);
                psI.setDouble(6, tienNuoc);
                psI.setDouble(7, phiDichVu);
                psI.setDouble(8, tongTien);
                psI.executeUpdate();
                return "OK";
            }
        } catch (SQLException e) {
            return "Lỗi SQL (Có thể trùng mã hóa đơn): " + e.getMessage();
        }
    }

    // Luồng 2.5.3: Ghi nhận thanh toán (Đã thanh toán / Thanh toán một phần)
    public static boolean ghiNhanThanhToan(String maGD, String maHoaDon, double soTienTra, String hinhThuc) {
        String sqlGet = "SELECT TongTien, DaThanhToan FROM HoaDon WHERE MaHoaDon = ?";
        String sqlUpd = "UPDATE HoaDon SET DaThanhToan = ?, TrangThaiThanhToan = ? WHERE MaHoaDon = ?";
        String sqlGD = "INSERT INTO GiaoDichThanhToan VALUES (?, ?, GETDATE(), ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            double tongTien = 0, daTra = 0;
            try (PreparedStatement psG = conn.prepareStatement(sqlGet)) {
                psG.setString(1, maHoaDon);
                ResultSet rs = psG.executeQuery();
                if (!rs.next()) return false;
                tongTien = rs.getDouble("TongTien");
                daTra = rs.getDouble("DaThanhToan");
            }

            double tongDaTraMoi = daTra + soTienTra;
            String trangThaiMoi = (tongDaTraMoi >= tongTien) ? "Đã thanh toán" : "Thanh toán một phần";

            try (PreparedStatement psU = conn.prepareStatement(sqlUpd);
                 PreparedStatement psLog = conn.prepareStatement(sqlGD)) {
                psU.setDouble(1, tongDaTraMoi);
                psU.setString(2, trangThaiMoi);
                psU.setString(3, maHoaDon);
                psU.executeUpdate();

                psLog.setString(1, maGD);
                psLog.setString(2, maHoaDon);
                psLog.setDouble(3, soTienTra);
                psLog.setString(4, hinhThuc);
                psLog.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) { return false; }
    }
}