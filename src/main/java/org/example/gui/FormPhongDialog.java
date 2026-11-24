package org.example.gui;

import org.example.dao.PhongTroDAO;
import org.example.exception.NgoaiLeNhapLieu;
import org.example.model.PhongTro;

import javax.swing.*;
import java.awt.*;

public class FormPhongDialog extends JDialog {
    private JTextField txtMaPhong, txtTenPhong, txtGiaThue;
    private JComboBox<String> cbTrangThai;
    private TrangChuFrame parentFrame;
    private PhongTro phongCu;

    public FormPhongDialog(TrangChuFrame parent, String title, PhongTro pt) {
        super(parent, title, true);
        this.parentFrame = parent;
        this.phongCu = pt;

        setSize(380, 250);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 10, 10));

        add(new JLabel("  Mã phòng:"));
        txtMaPhong = new JTextField();
        add(txtMaPhong);

        add(new JLabel("  Tên phòng:"));
        txtTenPhong = new JTextField();
        add(txtTenPhong);

        add(new JLabel("  Giá thuê:"));
        txtGiaThue = new JTextField();
        add(txtGiaThue);

        add(new JLabel("  Trạng thái:"));
        String[] cacTrangThai = {"Trống", "Đã thuê", "Đang sửa chữa"};
        cbTrangThai = new JComboBox<>(cacTrangThai);
        add(cbTrangThai);

        JLabel lblTrong = new JLabel("");
        JButton btnLuu = new JButton("Lưu thông tin");
        add(lblTrong);
        add(btnLuu);

        if (phongCu != null) {
            txtMaPhong.setText(phongCu.getMaPhong());
            txtMaPhong.setEditable(false);
            txtTenPhong.setText(phongCu.getTenPhong());
            txtGiaThue.setText(String.valueOf(phongCu.getGiaThue()));
            cbTrangThai.setSelectedItem(phongCu.getTrangThai());
        }

        btnLuu.addActionListener(e -> {
            try {
                String ma = txtMaPhong.getText().trim();
                String ten = txtTenPhong.getText().trim();
                String giaStr = txtGiaThue.getText().trim();
                String trangThai = (String) cbTrangThai.getSelectedItem();

                if (ma.isEmpty()) throw new NgoaiLeNhapLieu("Mã phòng không được để trống!");
                if (ten.isEmpty()) throw new NgoaiLeNhapLieu("Tên phòng không được để trống!");
                if (giaStr.isEmpty()) throw new NgoaiLeNhapLieu("Giá thuê không được để trống!");

                double gia;
                try {
                    gia = Double.parseDouble(giaStr);
                    if (gia < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    throw new NgoaiLeNhapLieu("Giá tiền không hợp lệ! Vui lòng chỉ nhập số dương.");
                }

                PhongTro phongMoi = new PhongTro(ma, ten, gia, trangThai);

                if (phongCu == null) {
                    if (PhongTroDAO.themPhong(phongMoi)) {
                        JOptionPane.showMessageDialog(this, "Thêm phòng thành công!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Thêm thất bại (Trùng mã phòng hoặc lỗi SQL)!");
                        return;
                    }
                } else {
                    if (PhongTroDAO.suaPhong(phongMoi)) {
                        JOptionPane.showMessageDialog(this, "Cập nhật phòng thành công!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                        return;
                    }
                }

                parentFrame.taiDuLieuTuSQL();
                dispose();

            } catch (NgoaiLeNhapLieu ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}