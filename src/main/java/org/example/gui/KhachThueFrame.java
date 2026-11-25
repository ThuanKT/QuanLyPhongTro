package org.example.gui;

import org.example.dao.DatabaseConnection;
import org.example.dao.NghiepVuDAO;
import org.example.dao.PhongTroDAO;
import org.example.model.HoaDon;
import org.example.model.PhongTro;
import org.example.model.TaiKhoan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class KhachThueFrame extends JFrame {
    private TaiKhoan taiKhoan;
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");

    private JComboBox<String> cbChonPhong;
    private JLabel lblThongTinHopDong;
    private DefaultTableModel modelHoaDon;
    private JTable tblHoaDon;
    private ArrayList<HoaDon> dsHoaDonPhong = new ArrayList<>();

    public KhachThueFrame(TaiKhoan tk) {
        this.taiKhoan = tk;
        setTitle("Cổng Thông Tin Khách Thuê - " + tk.getHoTen());
        setSize(950, 520);
        setMinimumSize(new Dimension(780, 420));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ==================== PANEL TRÊN (2 HÀNG CHỐNG CHỒNG CHỮ) ====================
        JPanel pnlTop = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        pnlTop.setBackground(new Color(240, 244, 248));

        // Hàng 1: Chọn phòng & Đăng xuất
        JPanel row1 = new JPanel(new BorderLayout(10, 0));
        row1.setOpaque(false);

        JPanel pnlSelectRoom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlSelectRoom.setOpaque(false);
        pnlSelectRoom.add(new JLabel("Chọn phòng của bạn:"));
        cbChonPhong = new JComboBox<>();
        cbChonPhong.setPreferredSize(new Dimension(220, 28));
        pnlSelectRoom.add(cbChonPhong);

        JButton btnXem = new JButton("Xem thông tin");
        pnlSelectRoom.add(btnXem);

        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.addActionListener(e -> {
            dispose();
            new DangNhapFrame().setVisible(true);
        });

        row1.add(pnlSelectRoom, BorderLayout.WEST);
        row1.add(btnDangXuat, BorderLayout.EAST);

        // Hàng 2: Thông tin hợp đồng của phòng đang chọn
        lblThongTinHopDong = new JLabel("Hợp đồng: Vui lòng chọn phòng để xem chi tiết.");
        lblThongTinHopDong.setFont(new Font("Arial", Font.BOLD, 13));
        lblThongTinHopDong.setForeground(new Color(0, 102, 204));

        pnlTop.add(row1);
        pnlTop.add(lblThongTinHopDong);
        add(pnlTop, BorderLayout.NORTH);

        // ==================== BẢNG HÓA ĐƠN CỦA PHÒNG ====================
        String[] cols = {"Mã HĐơn", "Tháng/Năm", "Tiền Phòng", "Tiền Điện", "Tiền Nước", "Dịch Vụ", "Tổng Tiền", "Đã Trả", "Còn Nợ", "Trạng Thái"};
        modelHoaDon = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHoaDon = new JTable(modelHoaDon);
        tblHoaDon.setRowHeight(26);
        add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);

        // ==================== PANEL NÚT THANH TOÁN DƯỚI CÙNG ====================
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnThanhToan = new JButton("Thanh Toán Hóa Đơn Đang Chọn");
        btnThanhToan.setBackground(new Color(39, 174, 96));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFont(new Font("Arial", Font.BOLD, 13));

        JButton btnTaiLai = new JButton("Làm Mới Dữ Liệu");
        pnlBottom.add(btnThanhToan);
        pnlBottom.add(btnTaiLai);
        add(pnlBottom, BorderLayout.SOUTH);

        // Tải danh sách phòng vào ComboBox
        taiDanhSachPhong();

        // Sự kiện đổi phòng / xem thông tin / làm mới
        btnXem.addActionListener(e -> taiDuLieuTheoPhong());
        cbChonPhong.addActionListener(e -> taiDuLieuTheoPhong());
        btnTaiLai.addActionListener(e -> taiDuLieuTheoPhong());

        // ==================== SỰ KIỆN KHÁCH THANH TOÁN ====================
        btnThanhToan.addActionListener(e -> xuLyKhachThanhToan());
    }

    private void taiDanhSachPhong() {
        cbChonPhong.removeAllItems();
        ArrayList<PhongTro> list = PhongTroDAO.layDanhSachPhong();
        for (PhongTro p : list) {
            cbChonPhong.addItem(p.getMaPhong() + " - " + p.getTenPhong());
        }
        if (cbChonPhong.getItemCount() > 0) {
            // Mặc định chọn P102 nếu có, hoặc phòng đầu tiên
            for (int i = 0; i < cbChonPhong.getItemCount(); i++) {
                if (cbChonPhong.getItemAt(i).startsWith("P102")) {
                    cbChonPhong.setSelectedIndex(i);
                    break;
                }
            }
            taiDuLieuTheoPhong();
        }
    }

    private String layMaPhongDangChon() {
        if (cbChonPhong.getSelectedItem() == null) return "";
        return cbChonPhong.getSelectedItem().toString().split(" - ")[0].trim();
    }

    private void taiDuLieuTheoPhong() {
        String maPhong = layMaPhongDangChon();
        if (maPhong.isEmpty()) return;

        // 1. Tải thông tin hợp đồng hiệu lực của phòng
        String sqlHD = "SELECT hd.MaHopDong, kt.HoTen, hd.NgayBatDau, hd.NgayKetThuc, hd.TienCoc, hd.TrangThai " +
                "FROM HopDong hd JOIN KhachThue kt ON hd.MaKhach = kt.MaKhach " +
                "WHERE hd.MaPhong = ? ORDER BY hd.NgayBatDau DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlHD)) {
            ps.setString(1, maPhong);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblThongTinHopDong.setText(String.format(
                        "Hợp đồng: %s | Khách thuê: %s | Thời hạn: %s đến %s | Tiền cọc: %s (%s)",
                        rs.getString("MaHopDong"),
                        rs.getString("HoTen"),
                        rs.getDate("NgayBatDau"),
                        rs.getDate("NgayKetThuc"),
                        df.format(rs.getDouble("TienCoc")),
                        rs.getString("TrangThai")
                ));
            } else {
                lblThongTinHopDong.setText("Phòng " + maPhong + " hiện chưa có hợp đồng thuê.");
            }
        } catch (Exception ex) {
            lblThongTinHopDong.setText("Lỗi tải thông tin hợp đồng: " + ex.getMessage());
        }

        // 2. Tải danh sách hóa đơn của phòng này
        modelHoaDon.setRowCount(0);
        dsHoaDonPhong.clear();
        for (HoaDon hd : NghiepVuDAO.layDanhSachHoaDon()) {
            if (hd.getMaPhong().equalsIgnoreCase(maPhong)) {
                dsHoaDonPhong.add(hd);
                double conNo = hd.getTongTien() - hd.getDaThanhToan();
                modelHoaDon.addRow(new Object[]{
                        hd.getMaHoaDon(),
                        hd.getThangNam(),
                        df.format(hd.getTienPhong()),
                        df.format(hd.getTienDien()),
                        df.format(hd.getTienNuoc()),
                        df.format(hd.getPhiDichVu()),
                        df.format(hd.getTongTien()),
                        df.format(hd.getDaThanhToan()),
                        df.format(Math.max(0, conNo)),
                        hd.getTrangThaiThanhToan()
                });
            }
        }
    }

    private void xuLyKhachThanhToan() {
        int row = tblHoaDon.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trên bảng để thanh toán!");
            return;
        }

        HoaDon hd = dsHoaDonPhong.get(row);
        if ("Đã thanh toán".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
            JOptionPane.showMessageDialog(this, "Hóa đơn " + hd.getMaHoaDon() + " đã được thanh toán đủ trước đó!");
            return;
        }

        double conNo = hd.getTongTien() - hd.getDaThanhToan();
        JTextField txtSoTien = new JTextField(String.valueOf((long) conNo));
        JComboBox<String> cbHinhThuc = new JComboBox<>(new String[]{"VietQR", "Chuyển khoản", "Tiền mặt"});

        Object[] fields = {
                "Mã hóa đơn: " + hd.getMaHoaDon() + " (Phòng " + hd.getMaPhong() + " - Tháng " + hd.getThangNam() + ")",
                "Số tiền còn nợ: " + df.format(conNo),
                "Nhập số tiền muốn thanh toán (VNĐ):", txtSoTien,
                "Chọn hình thức thanh toán:", cbHinhThuc
        };

        int opt = JOptionPane.showConfirmDialog(this, fields, "Thanh Toán Tiền Trọ Online", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                double tienTra = Double.parseDouble(txtSoTien.getText().trim());
                if (tienTra <= 0) {
                    JOptionPane.showMessageDialog(this, "Số tiền thanh toán phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (tienTra > conNo) {
                    JOptionPane.showMessageDialog(this, "Số tiền nhập vượt quá số nợ hiện tại (" + df.format(conNo) + ")!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String maGD = "GD" + (System.currentTimeMillis() % 100000);
                String hinhThuc = cbHinhThuc.getSelectedItem().toString();

                // Gọi chung hàm DAO của hệ thống -> Cập nhật thẳng vào SQL Server
                if (NghiepVuDAO.ghiNhanThanhToan(maGD, hd.getMaHoaDon(), tienTra, hinhThuc)) {
                    JOptionPane.showMessageDialog(this,
                            "Thanh toán thành công " + df.format(tienTra) + " qua " + hinhThuc + "!\n" +
                                    "Mã giao dịch: " + maGD + "\nHệ thống đã tự động cập nhật trạng thái hóa đơn cho Chủ trọ.");
                    taiDuLieuTheoPhong(); // Làm mới bảng bên khách ngay lập tức
                } else {
                    JOptionPane.showMessageDialog(this, "Giao dịch thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ (chỉ nhập số)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}