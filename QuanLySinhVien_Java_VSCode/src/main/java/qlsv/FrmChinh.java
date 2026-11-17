package qlsv;

import javax.swing.*;
import java.awt.*;

public class FrmChinh extends JFrame {
    public FrmChinh() {
        setTitle("PHAN MEM QUAN LY SINH VIEN");
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel title = new JLabel("PHAN MEM QUAN LY SINH VIEN", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(18, 10, 18, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Quan ly sinh vien", new FrmSinhVien());
        tabs.addTab("Quan ly lop", new FrmLop());
        tabs.addTab("Quan ly diem", new FrmKetQua());

        add(title, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }
}
