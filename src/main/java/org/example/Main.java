package org.example;

import org.example.gui.DangNhapFrame;
import javax.swing.*;
import java.security.MessageDigest;

public class Main {
    public static String maHoaCaesar(String dauVao) {
        int k = 3;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dauVao.length(); i++) {
            char c = dauVao.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append((char) ((c - 'A' + k) % 26 + 'A'));
            } else if (c >= 'a' && c <= 'z') {
                sb.append((char) ((c - 'a' + k) % 26 + 'a'));
            } else if (c >= '0' && c <= '9') {
                sb.append((char) ((c - '0' + k) % 10 + '0'));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DangNhapFrame().setVisible(true));
    }
}