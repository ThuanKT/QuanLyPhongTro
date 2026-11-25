package org.example.gui;

import org.example.Main;
import org.example.dao.PhongTroDAO;
import org.example.model.TaiKhoan;

import javax.swing.*;
import java.awt.*;

public class DangNhapFrame extends JFrame {
    public DangNhapFrame() {
        setTitle("Đăng nhập - Hệ thống Quản lý Khu Trọ");
        setSize(450, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2, 10, 10));

        JLabel lblTaiKhoan = new JLabel(" Tài khoản (admin / khach):");
        JTextField txtTaiKhoan = new JTextField("admin");
        JLabel lblMatKhau = new JLabel(" Mật khẩu (123456):");
        JPasswordField txtMatKhau = new JPasswordField("123456");
        JLabel lblNote = new JLabel(" Phân quyền hệ thống:");
        JLabel lblRoleInfo = new JLabel(" ChuTro | KhachThue");
        lblRoleInfo.setForeground(Color.BLUE);

        JButton btnDangNhap = new JButton("Đăng nhập");
        JButton btnThoat = new JButton("Thoát");

        add(lblTaiKhoan); add(txtTaiKhoan);
        add(lblMatKhau); add(txtMatKhau);
        add(lblNote); add(lblRoleInfo);
        add(btnThoat); add(btnDangNhap);

        btnDangNhap.addActionListener(e -> {
            String u = txtTaiKhoan.getText().trim();
            String p = new String(txtMatKhau.getPassword());
            String pMaHoa = Main.maHoaCaesar(p);

            TaiKhoan tk = PhongTroDAO.kiemTraDangNhap(u, pMaHoa);
            if (tk != null) {
                dispose();
                if ("KhachThue".equalsIgnoreCase(tk.getVaiTro())) {
                    new KhachThueFrame(tk).setVisible(true);
                } else {
                    new TrangChuFrame(tk).setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnThoat.addActionListener(e -> System.exit(0));
    }
}