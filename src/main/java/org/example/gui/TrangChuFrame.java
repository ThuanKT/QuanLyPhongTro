package org.example.gui;

import org.example.dao.NghiepVuDAO;
import org.example.dao.PhongTroDAO;
import org.example.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class TrangChuFrame extends JFrame {
    private TaiKhoan taiKhoanHienTai;
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");

    // Bảng & Model cho từng tab
    private DefaultTableModel modelPhong, modelHopDong, modelDienNuoc, modelHoaDon;
    private JTable tblPhong, tblHopDong, tblDienNuoc, tblHoaDon;
    private ArrayList<PhongTro> dsPhong = new ArrayList<>();
    private ArrayList<HopDong> dsHopDong = new ArrayList<>();
    private ArrayList<ChiSoDienNuoc> dsDienNuoc = new ArrayList<>();
    private ArrayList<HoaDon> dsHoaDon = new ArrayList<>();

    // Label thống kê (Dashboard Chủ trọ)
    private JLabel lblTongPhong, lblPhongTrong, lblTyLeLapDay, lblDoanhThu, lblCongNo;

    public TrangChuFrame(TaiKhoan tk) {
        this.taiKhoanHienTai = tk;
        setTitle("Hệ Thống Quản Lý Khu Trọ - Chủ trọ: " + tk.getHoTen());
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("1. Quản Lý Phòng Trọ", taoTabPhongTro());
        tabbedPane.addTab("2. Khách Thuê & Hợp Đồng", taoTabHopDong());
        tabbedPane.addTab("3. Điện Nước & IoT", taoTabDienNuoc());
        tabbedPane.addTab("4. Hóa Đơn & Thanh Toán", taoTabHoaDon());
        tabbedPane.addTab("5. Báo Cáo & Thống Kê", taoTabThongKe());

        add(tabbedPane, BorderLayout.CENTER);

        // Thanh trạng thái phía trên
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topBar.add(new JLabel("KHU NHÀ TRỌ TẬP TRUNG | Xin chào Chủ trọ: " + tk.getHoTen()), BorderLayout.WEST);
        JButton btnDangXuat = new JButton("Đăng xuất");
        btnDangXuat.addActionListener(e -> {
            dispose();
            new DangNhapFrame().setVisible(true);
        });
        topBar.add(btnDangXuat, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        taiToanBoDuLieu();
    }

    // ==================== TAB 1: QUẢN LÝ PHÒNG TRỌ ====================
    private JPanel taoTabPhongTro() {
        JPanel panel = new JPanel(new BorderLayout());
        modelPhong = new DefaultTableModel(new String[]{"Mã Phòng", "Tên Phòng", "Loại Phòng", "Giá Thuê Cơ Bản", "Trạng Thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPhong = new JTable(modelPhong);
        panel.add(new JScrollPane(tblPhong), BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnThem = new JButton("Thêm Phòng");
        JButton btnSua = new JButton("Sửa Phòng");
        JButton btnXoa = new JButton("Xóa Phòng");
        JButton btnTim = new JButton("Tìm Kiếm");
        JButton btnTaiLai = new JButton("Tải Lại");

        pnlBtn.add(btnThem); pnlBtn.add(btnSua); pnlBtn.add(btnXoa); pnlBtn.add(btnTim); pnlBtn.add(btnTaiLai);
        panel.add(pnlBtn, BorderLayout.SOUTH);

        btnThem.addActionListener(e -> new FormPhongDialog(this, "Thêm Phòng Mới", null).setVisible(true));
        btnSua.addActionListener(e -> {
            int row = tblPhong.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn phòng cần sửa!"); return; }
            new FormPhongDialog(this, "Sửa Thông Tin Phòng", dsPhong.get(row)).setVisible(true);
        });
        btnXoa.addActionListener(e -> {
            int row = tblPhong.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn phòng cần xóa!"); return; }
            String ma = dsPhong.get(row).getMaPhong();
            if (JOptionPane.showConfirmDialog(this, "Xóa phòng " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (PhongTroDAO.xoaPhong(ma)) taiToanBoDuLieu();
                else JOptionPane.showMessageDialog(this, "Không thể xóa phòng đang có hợp đồng hoặc hóa đơn liên kết!");
            }
        });
        btnTim.addActionListener(e -> {
            String kw = JOptionPane.showInputDialog(this, "Nhập mã hoặc tên phòng:");
            if (kw != null && !kw.trim().isEmpty()) {
                for (PhongTro p : dsPhong) {
                    if (p.getMaPhong().equalsIgnoreCase(kw.trim()) || p.getTenPhong().toLowerCase().contains(kw.trim().toLowerCase())) {
                        JOptionPane.showMessageDialog(this, "Tìm thấy: " + p.getMaPhong() + " - " + p.getTenPhong() + "\nGiá: " + df.format(p.getGiaThueCoBan()) + "\nTrạng thái: " + p.getTrangThai());
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this, "Không tìm thấy phòng phù hợp!");
            }
        });
        btnTaiLai.addActionListener(e -> taiToanBoDuLieu());
        return panel;
    }

    // ==================== TAB 2: KHÁCH THUÊ & HỢP ĐỒNG ====================
    private JPanel taoTabHopDong() {
        JPanel panel = new JPanel(new BorderLayout());
        modelHopDong = new DefaultTableModel(new String[]{"Mã HĐ", "Mã Phòng", "Mã Khách", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Tiền Cọc", "Trạng Thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHopDong = new JTable(modelHopDong);
        panel.add(new JScrollPane(tblHopDong), BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnLapHD = new JButton("Lập Hợp Đồng Thuê Mới");
        JButton btnSuaHD = new JButton("Sửa / Gia Hạn HĐ");
        JButton btnThanhLy = new JButton("Thanh Lý / Trả Phòng");
        JButton btnCanhBao = new JButton("Kiểm Tra HĐ Sắp Hết Hạn");

        pnlBtn.add(btnLapHD); pnlBtn.add(btnSuaHD); pnlBtn.add(btnThanhLy); pnlBtn.add(btnCanhBao);
        panel.add(pnlBtn, BorderLayout.SOUTH);

        btnLapHD.addActionListener(e -> {
            JTextField txtMaHD = new JTextField("HD0" + (dsHopDong.size() + 1));
            JTextField txtMaPhong = new JTextField("P101");
            JTextField txtMaKhach = new JTextField("KT0" + (dsHopDong.size() + 2));
            JTextField txtHoTen = new JTextField();
            JTextField txtCCCD = new JTextField();
            JTextField txtSDT = new JTextField();
            JTextField txtQue = new JTextField("Hà Nội");
            JTextField txtNgayBD = new JTextField("2026-11-25");
            JTextField txtNgayKT = new JTextField("2027-05-25");
            JTextField txtTienCoc = new JTextField("2000000");

            Object[] fields = {
                    "Mã Hợp Đồng:", txtMaHD, "Mã Phòng (Phải đang Trống):", txtMaPhong,
                    "Mã Khách Thuê:", txtMaKhach, "Họ Tên Khách:", txtHoTen,
                    "Số CCCD:", txtCCCD, "Số Điện Thoại:", txtSDT,
                    "Quê Quán:", txtQue, "Ngày Bắt Đầu (yyyy-MM-dd):", txtNgayBD,
                    "Ngày Kết Thúc (yyyy-MM-dd):", txtNgayKT, "Tiền Cọc:", txtTienCoc
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Lập Hợp Đồng Thuê Phòng", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    KhachThue kt = new KhachThue(txtMaKhach.getText().trim(), txtHoTen.getText().trim(), txtCCCD.getText().trim(), txtSDT.getText().trim(), txtQue.getText().trim());
                    HopDong hd = new HopDong(txtMaHD.getText().trim(), txtMaPhong.getText().trim(), kt.getMaKhach(), Date.valueOf(txtNgayBD.getText().trim()), Date.valueOf(txtNgayKT.getText().trim()), Double.parseDouble(txtTienCoc.getText().trim()), "Hiệu lực");
                    if (NghiepVuDAO.lapHopDongMoi(kt, hd)) {
                        JOptionPane.showMessageDialog(this, "Lập hợp đồng thành công! Phòng đã chuyển sang trạng thái 'Đang thuê'.");
                        taiToanBoDuLieu();
                    } else {
                        JOptionPane.showMessageDialog(this, "Thất bại! Phòng không tồn tại hoặc không ở trạng thái 'Trống'.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Dữ liệu ngày tháng hoặc số tiền không hợp lệ!");
                }
            }
        });

        btnSuaHD.addActionListener(e -> {
            int row = tblHopDong.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn hợp đồng trên bảng để sửa!");
                return;
            }
            HopDong hd = dsHopDong.get(row);
            JTextField txtNgayBD = new JTextField(hd.getNgayBatDau().toString());
            JTextField txtNgayKT = new JTextField(hd.getNgayKetThuc().toString());
            JTextField txtTienCoc = new JTextField(String.valueOf(hd.getTienCoc()));
            JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Hiệu lực", "Hết hạn", "Đã hủy"});
            cbTrangThai.setSelectedItem(hd.getTrangThai());

            Object[] fields = {
                    "Mã Hợp Đồng: " + hd.getMaHopDong() + " (Phòng " + hd.getMaPhong() + ")",
                    "Ngày Bắt Đầu (yyyy-MM-dd):", txtNgayBD,
                    "Ngày Kết Thúc / Gia Hạn (yyyy-MM-dd):", txtNgayKT,
                    "Tiền Cọc:", txtTienCoc,
                    "Trạng Thái Hợp Đồng:", cbTrangThai
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Sửa / Gia Hạn Hợp Đồng", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    boolean ok = NghiepVuDAO.suaHopDong(
                            hd.getMaHopDong(),
                            Date.valueOf(txtNgayBD.getText().trim()),
                            Date.valueOf(txtNgayKT.getText().trim()),
                            Double.parseDouble(txtTienCoc.getText().trim()),
                            (String) cbTrangThai.getSelectedItem()
                    );
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Cập nhật / Gia hạn hợp đồng thành công!");
                        taiToanBoDuLieu();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Dữ liệu ngày tháng (yyyy-MM-dd) hoặc tiền cọc không hợp lệ!");
                }
            }
        });

        btnThanhLy.addActionListener(e -> {
            int row = tblHopDong.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn hợp đồng cần thanh lý!"); return; }
            HopDong hd = dsHopDong.get(row);
            if (!"Hiệu lực".equals(hd.getTrangThai())) {
                JOptionPane.showMessageDialog(this, "Hợp đồng này đã kết thúc trước đó!");
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Thanh lý hợp đồng " + hd.getMaHopDong() + " và trả phòng " + hd.getMaPhong() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (NghiepVuDAO.thanhLyHopDong(hd.getMaHopDong(), hd.getMaPhong())) {
                    JOptionPane.showMessageDialog(this, "Đã thanh lý hợp đồng! Phòng " + hd.getMaPhong() + " đã trở lại trạng thái 'Trống'.");
                    taiToanBoDuLieu();
                }
            }
        });

        btnCanhBao.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Danh sách hợp đồng đang hiệu lực:\n");
            long now = System.currentTimeMillis();
            for (HopDong hd : dsHopDong) {
                if ("Hiệu lực".equals(hd.getTrangThai())) {
                    long daysLeft = (hd.getNgayKetThuc().getTime() - now) / (1000L * 60 * 60 * 24);
                    sb.append("- ").append(hd.getMaHopDong()).append(" (Phòng ").append(hd.getMaPhong()).append("): Còn ").append(daysLeft).append(" ngày hết hạn");
                    if (daysLeft <= 30) sb.append(" [CẢNH BÁO SẮP HẾT HẠN!]");
                    sb.append("\n");
                }
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Cảnh báo Hợp đồng", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    // ==================== TAB 3: ĐIỆN NƯỚC & IoT ====================
    private JPanel taoTabDienNuoc() {
        JPanel panel = new JPanel(new BorderLayout());
        modelDienNuoc = new DefaultTableModel(new String[]{"Mã CS", "Mã Phòng", "Tháng/Năm", "Điện Cũ", "Điện Mới", "Tiêu Thụ Điện", "Nước Cũ", "Nước Mới", "Tiêu Thụ Nước"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDienNuoc = new JTable(modelDienNuoc);
        panel.add(new JScrollPane(tblDienNuoc), BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnNhapTay = new JButton("Nhập Chỉ Số Thủ Công");
        JButton btnThuThapIoT = new JButton("Đồng Bộ Tự Động Từ Thiết Bị IoT");

        pnlBtn.add(btnNhapTay); pnlBtn.add(btnThuThapIoT);
        panel.add(pnlBtn, BorderLayout.SOUTH);

        btnNhapTay.addActionListener(e -> {
            JTextField txtMaCS = new JTextField("CS0" + (dsDienNuoc.size() + 1));
            JTextField txtPhong = new JTextField("P102");
            JTextField txtThang = new JTextField("12/2026");
            JTextField txtDienCu = new JTextField("1320");
            JTextField txtDienMoi = new JTextField("1450");
            JTextField txtNuocCu = new JTextField("52");
            JTextField txtNuocMoi = new JTextField("60");

            Object[] fields = {
                    "Mã Chỉ Số:", txtMaCS, "Mã Phòng:", txtPhong, "Tháng/Năm (MM/yyyy):", txtThang,
                    "Chỉ Số Điện Cũ:", txtDienCu, "Chỉ Số Điện Mới:", txtDienMoi,
                    "Chỉ Số Nước Cũ:", txtNuocCu, "Chỉ Số Nước Mới:", txtNuocMoi
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Ghi Chỉ Số Điện Nước", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    double dCu = Double.parseDouble(txtDienCu.getText().trim());
                    double dMoi = Double.parseDouble(txtDienMoi.getText().trim());
                    double nCu = Double.parseDouble(txtNuocCu.getText().trim());
                    double nMoi = Double.parseDouble(txtNuocMoi.getText().trim());

                    if (dMoi < dCu || nMoi < nCu) {
                        JOptionPane.showMessageDialog(this, "Cảnh báo lỗi: Chỉ số mới không được nhỏ hơn chỉ số cũ!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    ChiSoDienNuoc cs = new ChiSoDienNuoc(txtMaCS.getText().trim(), txtPhong.getText().trim(), txtThang.getText().trim(), dCu, dMoi, nCu, nMoi, null);
                    if (NghiepVuDAO.luuChiSoDienNuoc(cs)) {
                        JOptionPane.showMessageDialog(this, "Ghi nhận chỉ số thành công!");
                        taiToanBoDuLieu();
                    } else {
                        JOptionPane.showMessageDialog(this, "Lưu thất bại (Kiểm tra mã phòng hoặc trùng mã chỉ số)!");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!");
                }
            }
        });

        btnThuThapIoT.addActionListener(e -> {
            String maPhong = JOptionPane.showInputDialog(this, "Nhập Mã Phòng cần đọc công tơ điện/nước IoT (VD: P102):");
            String thang = JOptionPane.showInputDialog(this, "Nhập kỳ tháng/năm (VD: 12/2026):");
            if (maPhong != null && thang != null && !maPhong.trim().isEmpty()) {
                Random rand = new Random();
                double dienCu = 1320, nuocCu = 52;
                for (ChiSoDienNuoc c : dsDienNuoc) {
                    if (c.getMaPhong().equalsIgnoreCase(maPhong.trim())) {
                        dienCu = c.getChiSoDienMoi();
                        nuocCu = c.getChiSoNuocMoi();
                    }
                }
                double dienMoi = dienCu + 80 + rand.nextInt(70);
                double nuocMoi = nuocCu + 5 + rand.nextInt(8);
                ChiSoDienNuoc csIoT = new ChiSoDienNuoc("IOT" + System.currentTimeMillis() % 10000, maPhong.trim().toUpperCase(), thang.trim(), dienCu, dienMoi, nuocCu, nuocMoi, null);
                if (NghiepVuDAO.luuChiSoDienNuoc(csIoT)) {
                    JOptionPane.showMessageDialog(this, "HTTP 200 OK / ACK!\nĐã nhận dữ liệu từ công tơ IoT phòng " + maPhong + ":\n- Điện mới: " + dienMoi + " kWh\n- Nước mới: " + nuocMoi + " m3");
                    taiToanBoDuLieu();
                } else {
                    JOptionPane.showMessageDialog(this, "HTTP 400 Bad Request: Không tìm thấy phòng liên kết thiết bị IoT!");
                }
            }
        });

        return panel;
    }

    // ==================== TAB 4: HÓA ĐƠN & THANH TOÁN ====================
    private JPanel taoTabHoaDon() {
        JPanel panel = new JPanel(new BorderLayout());
        modelHoaDon = new DefaultTableModel(new String[]{"Mã HĐơn", "Phòng", "Tháng", "Tiền Phòng", "Tiền Điện", "Tiền Nước", "Dịch Vụ", "Tổng Tiền", "Đã Trả", "Trạng Thái"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblHoaDon = new JTable(modelHoaDon);
        panel.add(new JScrollPane(tblHoaDon), BorderLayout.CENTER);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnLapHoaDon = new JButton("Tự Động Tính & Lập Hóa Đơn");
        JButton btnThanhToan = new JButton("Ghi Nhận Thanh Toán");

        pnlBtn.add(btnLapHoaDon); pnlBtn.add(btnThanhToan);
        panel.add(pnlBtn, BorderLayout.SOUTH);

        btnLapHoaDon.addActionListener(e -> {
            JTextField txtMaHD = new JTextField("INV" + (dsHoaDon.size() + 1));
            JTextField txtPhong = new JTextField("P102");
            JTextField txtThang = new JTextField("11/2026");
            JTextField txtGiaDien = new JTextField("3500");
            JTextField txtGiaNuoc = new JTextField("20000");
            JTextField txtDichVu = new JTextField("150000");

            Object[] fields = {
                    "Mã Hóa Đơn:", txtMaHD, "Mã Phòng:", txtPhong, "Tháng/Năm:", txtThang,
                    "Đơn Giá Điện (VNĐ/kWh):", txtGiaDien, "Đơn Giá Nước (VNĐ/m3):", txtGiaNuoc,
                    "Phí Dịch Vụ (Wifi, Rác, Xe):", txtDichVu
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Tự Động Tính Hóa Đơn Hàng Tháng", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String res = NghiepVuDAO.tuDongLapHoaDon(
                            txtMaHD.getText().trim(), txtPhong.getText().trim(), txtThang.getText().trim(),
                            Double.parseDouble(txtGiaDien.getText().trim()),
                            Double.parseDouble(txtGiaNuoc.getText().trim()),
                            Double.parseDouble(txtDichVu.getText().trim())
                    );
                    if ("OK".equals(res)) {
                        JOptionPane.showMessageDialog(this, "Tự động tính và lập hóa đơn thành công!");
                        taiToanBoDuLieu();
                    } else {
                        JOptionPane.showMessageDialog(this, res, "Không thể lập hóa đơn", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Đơn giá nhập không hợp lệ!");
                }
            }
        });

        btnThanhToan.addActionListener(e -> {
            int row = tblHoaDon.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trên bảng để thanh toán!"); return; }
            HoaDon hd = dsHoaDon.get(row);
            if ("Đã thanh toán".equals(hd.getTrangThaiThanhToan())) {
                JOptionPane.showMessageDialog(this, "Hóa đơn này đã được thanh toán đủ!");
                return;
            }
            double conNo = hd.getTongTien() - hd.getDaThanhToan();
            JTextField txtSoTien = new JTextField(String.valueOf(conNo));
            JComboBox<String> cbHinhThuc = new JComboBox<>(new String[]{"Chuyển khoản", "Tiền mặt", "VietQR"});

            Object[] fields = {
                    "Hóa đơn: " + hd.getMaHoaDon() + " | Còn nợ: " + df.format(conNo),
                    "Số tiền khách trả:", txtSoTien,
                    "Hình thức thanh toán:", cbHinhThuc
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Ghi Nhận Thanh Toán", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    double tienTra = Double.parseDouble(txtSoTien.getText().trim());
                    if (tienTra <= 0) { JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0!"); return; }
                    String maGD = "GD" + System.currentTimeMillis() % 100000;
                    if (NghiepVuDAO.ghiNhanThanhToan(maGD, hd.getMaHoaDon(), tienTra, (String) cbHinhThuc.getSelectedItem())) {
                        JOptionPane.showMessageDialog(this, "Ghi nhận giao dịch thanh toán thành công!");
                        taiToanBoDuLieu();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ!");
                }
            }
        });

        return panel;
    }

    // ==================== TAB 5: BÁO CÁO & THỐNG KÊ (CHỦ TRỌ) ====================
    private JPanel taoTabThongKe() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        Font fTitle = new Font("Arial", Font.BOLD, 20);
        Font fStat = new Font("Arial", Font.PLAIN, 16);

        JLabel lblHeader = new JLabel("BÁO CÁO TỔNG QUAN HOẠT ĐỘNG KHU TRỌ", SwingConstants.CENTER);
        lblHeader.setFont(fTitle);
        lblHeader.setForeground(new Color(0, 102, 204));

        lblTongPhong = new JLabel(); lblTongPhong.setFont(fStat);
        lblPhongTrong = new JLabel(); lblPhongTrong.setFont(fStat);
        lblTyLeLapDay = new JLabel(); lblTyLeLapDay.setFont(fStat);
        lblDoanhThu = new JLabel(); lblDoanhThu.setFont(fStat); lblDoanhThu.setForeground(new Color(0, 153, 51));
        lblCongNo = new JLabel(); lblCongNo.setFont(fStat); lblCongNo.setForeground(Color.RED);

        panel.add(lblHeader);
        panel.add(lblTongPhong);
        panel.add(lblPhongTrong);
        panel.add(lblTyLeLapDay);
        panel.add(lblDoanhThu);
        panel.add(lblCongNo);

        return panel;
    }

    // ==================== TẢI DỮ LIỆU & CẬP NHẬT GIAO DIỆN ====================
    public void taiToanBoDuLieu() {
        dsPhong = PhongTroDAO.layDanhSachPhong();
        dsHopDong = NghiepVuDAO.layDanhSachHopDong();
        dsDienNuoc = NghiepVuDAO.layDanhSachChiSo();
        dsHoaDon = NghiepVuDAO.layDanhSachHoaDon();

        modelPhong.setRowCount(0);
        int soPhongTrong = 0, soPhongThue = 0;
        for (PhongTro p : dsPhong) {
            modelPhong.addRow(new Object[]{p.getMaPhong(), p.getTenPhong(), p.getLoaiPhong(), df.format(p.getGiaThueCoBan()), p.getTrangThai()});
            if ("Trống".equals(p.getTrangThai())) soPhongTrong++;
            if ("Đang thuê".equals(p.getTrangThai())) soPhongThue++;
        }

        modelHopDong.setRowCount(0);
        for (HopDong h : dsHopDong) {
            modelHopDong.addRow(new Object[]{h.getMaHopDong(), h.getMaPhong(), h.getMaKhach(), h.getNgayBatDau(), h.getNgayKetThuc(), df.format(h.getTienCoc()), h.getTrangThai()});
        }

        modelDienNuoc.setRowCount(0);
        for (ChiSoDienNuoc c : dsDienNuoc) {
            modelDienNuoc.addRow(new Object[]{c.getMaChiSo(), c.getMaPhong(), c.getThangNam(), c.getChiSoDienCu(), c.getChiSoDienMoi(), c.tinhLuongDienTieuThu() + " kWh", c.getChiSoNuocCu(), c.getChiSoNuocMoi(), c.tinhLuongNuocTieuThu() + " m3"});
        }

        modelHoaDon.setRowCount(0);
        double tongDoanhThu = 0, tongCongNo = 0;
        for (HoaDon hd : dsHoaDon) {
            modelHoaDon.addRow(new Object[]{
                    hd.getMaHoaDon(), hd.getMaPhong(), hd.getThangNam(),
                    df.format(hd.getTienPhong()), df.format(hd.getTienDien()), df.format(hd.getTienNuoc()),
                    df.format(hd.getPhiDichVu()), df.format(hd.getTongTien()), df.format(hd.getDaThanhToan()),
                    hd.getTrangThaiThanhToan()
            });
            tongDoanhThu += hd.getDaThanhToan();
            tongCongNo += (hd.getTongTien() - hd.getDaThanhToan());
        }

        // Cập nhật số liệu tổng quan cho Chủ trọ
        int tong = dsPhong.size();
        double tyLe = (tong == 0) ? 0 : ((double) soPhongThue / tong) * 100;
        lblTongPhong.setText("• Tổng số phòng trong khu trọ: " + tong + " phòng");
        lblPhongTrong.setText("• Số phòng đang trống sẵn sàng cho thuê: " + soPhongTrong + " phòng");
        lblTyLeLapDay.setText(String.format("• Tỷ lệ lấp đầy phòng hiện tại: %.1f%% (%d/%d phòng đang thuê)", tyLe, soPhongThue, tong));
        lblDoanhThu.setText("• Tổng doanh thu thực tế đã thu được: " + df.format(tongDoanhThu));
        lblCongNo.setText("• Tổng công nợ chưa thanh toán: " + df.format(tongCongNo));
    }
}