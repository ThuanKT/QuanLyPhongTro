package org.example.gui;

import org.example.dao.PhongTroDAO;
import org.example.model.PhongTro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class TrangChuFrame extends JFrame {
    DefaultTableModel model;
    JTable table;
    ArrayList<PhongTro> danhSachPhong = new ArrayList<>();

    public TrangChuFrame() {
        setTitle("Trang Chủ - Quản Lý Phòng Trọ");
        setSize(850, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Mã Phòng", "Tên Phòng", "Giá Thuê", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnThem = new JButton("Thêm Phòng");
        JButton btnSua = new JButton("Sửa Phòng");
        JButton btnXoa = new JButton("Xóa Phòng");
        JButton btnTimKiem = new JButton("Tìm Kiếm");
        JButton btnTaiLai = new JButton("Tải Lại");

        btnThem.setMnemonic(KeyEvent.VK_T);
        btnSua.setMnemonic(KeyEvent.VK_S);
        btnXoa.setMnemonic(KeyEvent.VK_X);
        btnTimKiem.setMnemonic(KeyEvent.VK_F);

        panelButtons.add(btnThem);
        panelButtons.add(btnSua);
        panelButtons.add(btnXoa);
        panelButtons.add(btnTimKiem);
        panelButtons.add(btnTaiLai);

        add(panelButtons, BorderLayout.SOUTH);

        new Thread(this::taiDuLieuTuSQL).start();

        btnThem.addActionListener(e -> new FormPhongDialog(this, "Thêm Phòng Mới", null).setVisible(true));

        btnSua.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Hãy chọn một phòng trên bảng để sửa!");
                return;
            }
            PhongTro ptChon = danhSachPhong.get(row);
            new FormPhongDialog(this, "Sửa Thông Tin Phòng", ptChon).setVisible(true);
        });

        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Hãy chọn một phòng trên bảng để xóa!");
                return;
            }
            String maCanXoa = danhSachPhong.get(row).getMaPhong();
            int xacNhan = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phòng " + maCanXoa + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (xacNhan == JOptionPane.YES_OPTION) {
                boolean thanhCong = PhongTroDAO.xoaPhong(maCanXoa);
                if (thanhCong) {
                    taiDuLieuTuSQL();
                    JOptionPane.showMessageDialog(this, "Xóa thành công!");
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi xóa phòng!");
                }
            }
        });

        btnTimKiem.addActionListener(e -> {
            String tuKhoa = JOptionPane.showInputDialog(this, "Nhập mã phòng cần tìm:");
            if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
                for (PhongTro pt : danhSachPhong) {
                    if (pt.getMaPhong().equalsIgnoreCase(tuKhoa.trim())) {
                        JOptionPane.showMessageDialog(this, "Tìm thấy!\nMã phòng: " + pt.getMaPhong() + "\nTên phòng: " + pt.getTenPhong() + "\nGiá thuê: " + pt.getGiaThue() + "\nTrạng thái: " + pt.getTrangThai());
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this, "Không tìm thấy phòng có mã: " + tuKhoa);
            }
        });

        btnTaiLai.addActionListener(e -> taiDuLieuTuSQL());
    }

    public void taiDuLieuTuSQL() {
        danhSachPhong = PhongTroDAO.layDanhSachPhong();
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (PhongTro pt : danhSachPhong) {
                model.addRow(new Object[]{pt.getMaPhong(), pt.getTenPhong(), pt.getGiaThue(), pt.getTrangThai()});
            }
        });
    }
}