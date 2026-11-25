package org.example.gui;

import org.example.dao.DatabaseConnection;
import org.example.model.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class KhachThueFrame extends JFrame {
    private TaiKhoan taiKhoan;
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private DefaultTableModel model;
    private JLabel lblThongTinPhong;
    private JComboBox<String> cbChonPhong;

    public KhachThueFrame(TaiKhoan tk) {
        this.taiKhoan = tk;

        // 1. Lấy danh sách phòng từ SQL Server
        List<String> dsPhong = layDanhSachPhong();
        if (dsPhong.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Chưa có dữ liệu phòng trọ trong hệ thống!");
            new DangNhapFrame().setVisible(true);
            return;
        }

        // 2. Hiện bảng chọn phòng ngay khi đăng nhập thành công
        String phongDuocChon = (String) JOptionPane.showInputDialog(
                null,
                "Đăng nhập thành công!\nVui lòng chọn phòng của bạn để tra cứu thông tin:",
                "Chọn Phòng Trọ",
                JOptionPane.QUESTION_MESSAGE,
                null,
                dsPhong.toArray(),
                dsPhong.get(0)
        );

        // Nếu khách bấm Cancel thì quay lại màn hình đăng nhập
        if (phongDuocChon == null) {
            new DangNhapFrame().setVisible(true);
            return;
        }

        setTitle("Cổng Thông Tin Khách Thuê Trọ");
        setSize(950, 520);
        setMinimumSize(new Dimension(780, 420)); // Chống thu nhỏ quá mức làm đè chữ
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Thanh tiêu đề chia 2 dòng (GridLayout 2 hàng, 1 cột) để không bao giờ bị đè chữ
        JPanel pnlTop = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlTop.setBackground(new Color(41, 128, 185));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Dòng 1: Thông tin chi tiết phòng & hợp đồng
        lblThongTinPhong = new JLabel();
        lblThongTinPhong.setForeground(Color.WHITE);
        lblThongTinPhong.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Dòng 2: Thanh công cụ chọn phòng & đăng xuất
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlControls.setOpaque(false);

        JLabel lblChon = new JLabel("Đổi phòng xem:");
        lblChon.setForeground(Color.WHITE);
        lblChon.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cbChonPhong = new JComboBox<>(dsPhong.toArray(new String[0]));
        cbChonPhong.setSelectedItem(phongDuocChon);

        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.addActionListener(e -> {
            dispose();
            new DangNhapFrame().setVisible(true);
        });

        pnlControls.add(lblChon);
        pnlControls.add(cbChonPhong);
        pnlControls.add(btnDangXuat);

        pnlTop.add(lblThongTinPhong);
        pnlTop.add(pnlControls);

        // Bảng danh sách hóa đơn
        String[] cols = {"Mã Hóa Đơn", "Tháng/Năm", "Tiền Phòng", "Tiền Điện", "Tiền Nước", "Phí Dịch Vụ", "Tổng Tiền", "Trạng Thái Thanh Toán"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblHoaDon = new JTable(model);
        tblHoaDon.setRowHeight(26);

        // Sự kiện khi chọn phòng trong ComboBox
        cbChonPhong.addActionListener(e -> {
            String item = (String) cbChonPhong.getSelectedItem();
            if (item != null) {
                String maPhong = item.split(" - ")[0].trim();
                taiDuLieuTheoPhong(maPhong);
            }
        });

        // Tải dữ liệu cho phòng vừa chọn
        String maPhongBanDau = phongDuocChon.split(" - ")[0].trim();
        taiDuLieuTheoPhong(maPhongBanDau);

        add(pnlTop, BorderLayout.NORTH);
        add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);
    }

    private List<String> layDanhSachPhong() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT p.MaPhong, p.TenPhong, p.TrangThai, k.HoTen " +
                "FROM PhongTro p " +
                "LEFT JOIN HopDong h ON p.MaPhong = h.MaPhong AND h.TrangThai = N'Hiệu lực' " +
                "LEFT JOIN KhachThue k ON h.MaKhach = k.MaKhach " +
                "ORDER BY p.MaPhong";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String ma = rs.getString("MaPhong");
                String ten = rs.getString("TenPhong");
                String khach = rs.getString("HoTen");
                String trangThai = rs.getString("TrangThai");
                String hienThi = ma + " - " + ten + (khach != null ? " (Khách: " + khach + ")" : " [" + trangThai + "]");
                list.add(hienThi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void taiDuLieuTheoPhong(String maPhong) {
        // Cập nhật thông tin hợp đồng & khách thuê lên thanh tiêu đề
        String sqlInfo = "SELECT p.TenPhong, k.HoTen, h.NgayKetThuc " +
                "FROM PhongTro p " +
                "LEFT JOIN HopDong h ON p.MaPhong = h.MaPhong AND h.TrangThai = N'Hiệu lực' " +
                "LEFT JOIN KhachThue k ON h.MaKhach = k.MaKhach " +
                "WHERE p.MaPhong = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlInfo)) {
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String tenPhong = rs.getString("TenPhong");
                String hoTenKhach = rs.getString("HoTen");
                Date ngayKT = rs.getDate("NgayKetThuc");
                if (hoTenKhach != null) {
                    lblThongTinPhong.setText("Phòng: " + maPhong + " (" + tenPhong + ")  |  Khách thuê: " + hoTenKhach + "  |  Hạn HĐ: " + ngayKT);
                } else {
                    lblThongTinPhong.setText("Phòng: " + maPhong + " (" + tenPhong + ")  |  Hiện chưa có hợp đồng hiệu lực");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cập nhật bảng hóa đơn của phòng
        model.setRowCount(0);
        String sqlHD = "SELECT * FROM HoaDon WHERE MaPhong = ? ORDER BY ThangNam DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlHD)) {
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("MaHoaDon"),
                        rs.getString("ThangNam"),
                        df.format(rs.getDouble("TienPhong")),
                        df.format(rs.getDouble("TienDien")),
                        df.format(rs.getDouble("TienNuoc")),
                        df.format(rs.getDouble("PhiDichVu")),
                        df.format(rs.getDouble("TongTien")),
                        rs.getString("TrangThaiThanhToan")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}