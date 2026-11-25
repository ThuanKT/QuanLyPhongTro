package org.example.gui;

import org.example.dao.PhongTroDAO;
import org.example.exception.NgoaiLeNhapLieu;
import org.example.model.PhongTro;

import javax.swing.*;
import java.awt.*;

public class FormPhongDialog extends JDialog {
    private JTextField txtMaPhong, txtTenPhong, txtLoaiPhong, txtGiaThue;
    private JComboBox<String> cbTrangThai;
    private TrangChuFrame parentFrame;
    private PhongTro phongCu;

    public FormPhongDialog(TrangChuFrame parent, String title, PhongTro pt) {
        super(parent, title, true);
        this.parentFrame = parent;
        this.phongCu = pt;

        setSize(400, 290);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(6, 2, 10, 10));

        add(new JLabel("  Mã phòng:"));
        txtMaPhong = new JTextField();
        add(txtMaPhong);

        add(new JLabel("  Tên phòng:"));
        txtTenPhong = new JTextField();
        add(txtTenPhong);

        add(new JLabel("  Loại phòng:"));
        txtLoaiPhong = new JTextField("Phòng đơn khép kín");
        add(txtLoaiPhong);

        add(new JLabel("  Giá thuê cơ bản:"));
        txtGiaThue = new JTextField();
        add(txtGiaThue);

        add(new JLabel("  Trạng thái:"));
        // Chuẩn theo Biểu đồ trạng thái 2.6.1 của báo cáo
        String[] cacTrangThai = {"Trống", "Đang thuê", "Đang sửa"};
        cbTrangThai = new JComboBox<>(cacTrangThai);
        add(cbTrangThai);

        add(new JLabel(""));
        JButton btnLuu = new JButton("Lưu thông tin");
        add(btnLuu);

        if (phongCu != null) {
            txtMaPhong.setText(phongCu.getMaPhong());
            txtMaPhong.setEditable(false);
            txtTenPhong.setText(phongCu.getTenPhong());
            txtLoaiPhong.setText(phongCu.getLoaiPhong());
            txtGiaThue.setText(String.valueOf(phongCu.getGiaThueCoBan()));
            cbTrangThai.setSelectedItem(phongCu.getTrangThai());
        }

        btnLuu.addActionListener(e -> {
            try {
                String ma = txtMaPhong.getText().trim();
                String ten = txtTenPhong.getText().trim();
                String loai = txtLoaiPhong.getText().trim();
                String giaStr = txtGiaThue.getText().trim();
                String trangThai = (String) cbTrangThai.getSelectedItem();

                if (ma.isEmpty() || ten.isEmpty() || loai.isEmpty() || giaStr.isEmpty()) {
                    throw new NgoaiLeNhapLieu("Vui lòng nhập đầy đủ thông tin phòng!");
                }

                double gia;
                try {
                    gia = Double.parseDouble(giaStr);
                    if (gia <= 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    throw new NgoaiLeNhapLieu("Giá thuê phải là số dương!");
                }

                PhongTro phongMoi = new PhongTro(ma, ten, loai, gia, trangThai);
                boolean ok = (phongCu == null) ? PhongTroDAO.themPhong(phongMoi) : PhongTroDAO.suaPhong(phongMoi);

                if (ok) {
                    JOptionPane.showMessageDialog(this, "Lưu thông tin phòng thành công!");
                    parentFrame.taiToanBoDuLieu();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại (Kiểm tra trùng mã phòng)!");
                }
            } catch (NgoaiLeNhapLieu ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}