package org.example.gui;

import org.example.Main;
import javax.swing.*;
import java.awt.*;

public class DangNhapFrame extends JFrame {
    public DangNhapFrame() {
        setTitle("Đăng nhập hệ thống");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        String matKhauGocDaMaHoa = Main.maHoaCaesar("123456");

        JLabel lblTaiKhoan = new JLabel(" Tài khoản (admin):");
        JTextField txtTaiKhoan = new JTextField();
        JLabel lblMatKhau = new JLabel(" Mật khẩu (123456):");
        JPasswordField txtMatKhau = new JPasswordField();
        JButton btnDangNhap = new JButton("Đăng nhập");
        JButton btnThoat = new JButton("Thoát");

        add(lblTaiKhoan);
        add(txtTaiKhoan);
        add(lblMatKhau);
        add(txtMatKhau);
        add(btnThoat);
        add(btnDangNhap);

        btnDangNhap.addActionListener(e -> {
            String u = txtTaiKhoan.getText();
            String p = new String(txtMatKhau.getPassword());
            String pMaHoa = Main.maHoaCaesar(p);

            if (u.equals("admin") && pMaHoa.equals(matKhauGocDaMaHoa)) {
                dispose();
                new TrangChuFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!");
            }
        });

        btnThoat.addActionListener(e -> System.exit(0));
    }
}