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
        setTitle("Cổng Thông Tin Khách Thuê Ký Túc Xá - " + tk.getHoTen());
        setSize(980, 540);
        setMinimumSize(new Dimension(800, 440));
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
        pnlSelectRoom.add(new JLabel("Chọn phòng KTX của bạn:"));
        cbChonPhong = new JComboBox<>();
        cbChonPhong.setPreferredSize(new Dimension(240, 28));
        pnlSelectRoom.add(cbChonPhong);

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

        // ==================== PANEL NÚT CHỨC NĂNG DƯỚI CÙNG ====================
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnChiTiet = new JButton("Xem Chi Tiết Hóa Đơn");
        btnChiTiet.setBackground(new Color(41, 128, 185));
        btnChiTiet.setForeground(Color.WHITE);
        btnChiTiet.setFont(new Font("Arial", Font.BOLD, 13));

        JButton btnThanhToan = new JButton("Thanh Toán Hóa Đơn Đang Chọn");
        btnThanhToan.setBackground(new Color(39, 174, 96));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFont(new Font("Arial", Font.BOLD, 13));

        JButton btnTaiLai = new JButton("Làm Mới Dữ Liệu");

        pnlBottom.add(btnChiTiet);
        pnlBottom.add(btnThanhToan);
        pnlBottom.add(btnTaiLai);
        add(pnlBottom, BorderLayout.SOUTH);

        // Tải danh sách phòng vào ComboBox
        taiDanhSachPhong();

        // Sự kiện đổi phòng / làm mới / xem chi tiết / thanh toán
        cbChonPhong.addActionListener(e -> taiDuLieuTheoPhong());
        btnTaiLai.addActionListener(e -> taiDuLieuTheoPhong());
        btnChiTiet.addActionListener(e -> xemChiTietHoaDon());
        btnThanhToan.addActionListener(e -> xuLyKhachThanhToan());
    }

    private void taiDanhSachPhong() {
        cbChonPhong.removeAllItems();
        ArrayList<PhongTro> list = PhongTroDAO.layDanhSachPhong();
        for (PhongTro p : list) {
            cbChonPhong.addItem(p.getMaPhong() + " - " + p.getTenPhong());
        }
        if (cbChonPhong.getItemCount() > 0) {
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
                String trangThaiHienThi = "Chờ xác nhận".equalsIgnoreCase(hd.getTrangThaiThanhToan())
                        ? "Chờ quản lý xác nhận"
                        : hd.getTrangThaiThanhToan();

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
                        trangThaiHienThi
                });
            }
        }
    }

    // ==================== XEM CHI TIẾT HÓA ĐƠN (CHỈ SỐ ĐIỆN NƯỚC & KHOẢN PHÍ) ====================
    private void xemChiTietHoaDon() {
        int row = tblHoaDon.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trên bảng để xem chi tiết!");
            return;
        }

        HoaDon hd = dsHoaDonPhong.get(row);
        double conNo = Math.max(0, hd.getTongTien() - hd.getDaThanhToan());
        String trangThaiHienThi = "Chờ xác nhận".equalsIgnoreCase(hd.getTrangThaiThanhToan())
                ? "Chờ quản lý xác nhận"
                : hd.getTrangThaiThanhToan();

        // Truy vấn chỉ số điện nước chi tiết của phòng trong tháng đó
        String thongTinDienNuoc = "   (Chưa có dữ liệu chốt chỉ số công tơ chi tiết)";
        String sqlCS = "SELECT ChiSoDienCu, ChiSoDienMoi, ChiSoNuocCu, ChiSoNuocMoi, NgayGhi " +
                "FROM ChiSoDienNuoc WHERE MaPhong = ? AND ThangNam = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlCS)) {
            ps.setString(1, hd.getMaPhong());
            ps.setString(2, hd.getThangNam());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double dCu = rs.getDouble("ChiSoDienCu");
                double dMoi = rs.getDouble("ChiSoDienMoi");
                double nCu = rs.getDouble("ChiSoNuocCu");
                double nMoi = rs.getDouble("ChiSoNuocMoi");
                double soDien = dMoi - dCu;
                double soNuoc = nMoi - nCu;
                double donGiaDien = (soDien > 0) ? (hd.getTienDien() / soDien) : 0;
                double donGiaNuoc = (soNuoc > 0) ? (hd.getTienNuoc() / soNuoc) : 0;

                thongTinDienNuoc = String.format(
                        "   • Điện tiêu thụ: %.0f kWh (Chỉ số cũ: %.0f → Mới: %.0f | Đơn giá: %s/kWh)\n" +
                                "   • Nước tiêu thụ: %.0f m³ (Chỉ số cũ: %.0f → Mới: %.0f | Đơn giá: %s/m³)\n" +
                                "   • Ngày chốt số: %s",
                        soDien, dCu, dMoi, df.format(donGiaDien),
                        soNuoc, nCu, nMoi, df.format(donGiaNuoc),
                        rs.getDate("NgayGhi") != null ? rs.getDate("NgayGhi").toString() : "Cuối kỳ"
                );
            }
        } catch (Exception ignored) {}

        // Truy vấn lịch sử giao dịch thanh toán của hóa đơn này
        StringBuilder lichSuGD = new StringBuilder();
        String sqlGD = "SELECT MaGiaoDich, NgayThanhToan, SoTienTra, HinhThuc FROM GiaoDichThanhToan WHERE MaHoaDon = ? ORDER BY NgayThanhToan DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlGD)) {
            ps.setString(1, hd.getMaHoaDon());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lichSuGD.append(String.format("   - [%s] %s | Số tiền: %s | Hình thức: %s\n",
                        rs.getString("MaGiaoDich"),
                        rs.getTimestamp("NgayThanhToan"),
                        df.format(rs.getDouble("SoTienTra")),
                        rs.getString("HinhThuc")));
            }
        } catch (Exception ignored) {}
        if (lichSuGD.length() == 0) {
            lichSuGD.append("   (Chưa phát sinh giao dịch thanh toán nào)\n");
        }

        // Tạo nội dung phiếu chi tiết hiển thị trong hộp thoại
        String noiDung = String.format(
                "==================== PHIẾU CHI TIẾT HÓA ĐƠN KÝ TÚC XÁ ====================\n" +
                        "Mã hóa đơn : %-16s | Phòng KTX : %s\n" +
                        "Kỳ hóa đơn : Tháng %-10s | Trạng thái: %s\n" +
                        "-----------------------------------------------------------------------------\n" +
                        "1. CHI TIẾT CHỈ SỐ ĐIỆN & NƯỚC:\n%s\n\n" +
                        "2. BẢNG KÊ CÁC KHOẢN PHÍ PHẢI ĐÓNG:\n" +
                        "   • Tiền thuê phòng KTX cố định       : %s\n" +
                        "   • Tiền điện sinh hoạt               : %s\n" +
                        "   • Tiền nước sinh hoạt               : %s\n" +
                        "   • Phí dịch vụ (Wifi, rác, vệ sinh)  : %s\n" +
                        "   --------------------------------------------------------------------------\n" +
                        "   => TỔNG CỘNG PHẢI THANH TOÁN        : %s\n" +
                        "   => ĐÃ THANH TOÁN (ĐÃ DUYỆT)         : %s\n" +
                        "   => SỐ TIỀN CÒN NỢ                   : %s\n" +
                        "-----------------------------------------------------------------------------\n" +
                        "3. LỊCH SỬ GIAO DỊCH THANH TOÁN:\n%s" +
                        "=============================================================================",
                hd.getMaHoaDon(), hd.getMaPhong(),
                hd.getThangNam(), trangThaiHienThi,
                thongTinDienNuoc,
                df.format(hd.getTienPhong()),
                df.format(hd.getTienDien()),
                df.format(hd.getTienNuoc()),
                df.format(hd.getPhiDichVu()),
                df.format(hd.getTongTien()),
                df.format(hd.getDaThanhToan()),
                df.format(conNo),
                lichSuGD.toString()
        );

        JTextArea txtChiTiet = new JTextArea(noiDung);
        txtChiTiet.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtChiTiet.setEditable(false);
        txtChiTiet.setBackground(new Color(250, 250, 250));
        txtChiTiet.setMargin(new Insets(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(txtChiTiet);
        scroll.setPreferredSize(new Dimension(640, 420));

        JOptionPane.showMessageDialog(this, scroll, "Chi Tiết Hóa Đơn " + hd.getMaHoaDon(), JOptionPane.INFORMATION_MESSAGE);
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
        if ("Chờ xác nhận".equalsIgnoreCase(hd.getTrangThaiThanhToan())) {
            JOptionPane.showMessageDialog(this, "Hóa đơn " + hd.getMaHoaDon() + " đang chờ quản lý KTX xác nhận thanh toán!");
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

        int opt = JOptionPane.showConfirmDialog(this, fields, "Thanh Toán Phí Ký Túc Xá Online", JOptionPane.OK_CANCEL_OPTION);
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

                if (NghiepVuDAO.guiYeuCauThanhToan(maGD, hd.getMaHoaDon(), tienTra, hinhThuc)) {
                    JOptionPane.showMessageDialog(this,
                            "Đã gửi xác nhận thanh toán " + df.format(tienTra) + " qua " + hinhThuc + "!\n" +
                                    "Mã giao dịch: " + maGD + "\nTrạng thái hóa đơn đã chuyển sang: Chờ quản lý xác nhận.");
                    taiDuLieuTheoPhong();
                } else {
                    JOptionPane.showMessageDialog(this, "Giao dịch thất bại, vui lòng thử lại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ (chỉ nhập số)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}