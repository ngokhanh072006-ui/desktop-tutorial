package qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FrmKetQua extends JPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtMaSV = new JTextField();
    private final JTextField txtMon = new JTextField();
    private final JTextField txtDiem = new JTextField();
    private final JTextField txtTim = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Ma ket qua", "Ma SV", "Ho ten", "Mon hoc", "Diem"}, 0);
    private final JTable table = new JTable(model);

    public FrmKetQua() {
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(2, 4, 8, 5));
        form.add(new JLabel("Ma ket qua")); form.add(new JLabel("Ma sinh vien"));
        form.add(new JLabel("Mon hoc")); form.add(new JLabel("Diem (0-10)"));
        form.add(txtMa); txtMa.setEditable(false);
        form.add(txtMaSV); form.add(txtMon); form.add(txtDiem);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Them diem"), update = new JButton("Sua diem");
        JButton delete = new JButton("Xoa diem"), clear = new JButton("Lam moi");
        JButton search = new JButton("Tim kiem");
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(clear);
        buttons.add(new JLabel("Tim ma SV:")); buttons.add(txtTim); buttons.add(search);
        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(form, BorderLayout.NORTH); top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> them()); update.addActionListener(e -> sua());
        delete.addActionListener(e -> xoa()); clear.addActionListener(e -> lamMoi());
        search.addActionListener(e -> taiDuLieu(txtTim.getText().trim()));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                txtMa.setText(model.getValueAt(r, 0).toString());
                txtMaSV.setText(model.getValueAt(r, 1).toString());
                txtMon.setText(model.getValueAt(r, 3).toString());
                txtDiem.setText(model.getValueAt(r, 4).toString());
            }
        });
        taiDuLieu("");
    }

    private void taiDuLieu(String key) {
        model.setRowCount(0);
        String sql = "SELECT k.ma_kq,k.ma_sv,s.ho_ten,k.mon_hoc,k.diem FROM ket_qua k JOIN sinh_vien s ON k.ma_sv=s.ma_sv WHERE k.ma_sv LIKE ? ORDER BY k.ma_kq";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + key + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) model.addRow(new Object[]{rs.getInt("ma_kq"), rs.getString("ma_sv"),
                    rs.getString("ho_ten"), rs.getString("mon_hoc"), rs.getBigDecimal("diem")});
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private boolean kiemTra() {
        if (txtMaSV.getText().trim().isEmpty() || txtMon.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhap ma sinh vien va mon hoc."); return false;
        }
        try {
            double d = Double.parseDouble(txtDiem.getText().trim());
            if (d < 0 || d > 10) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Diem phai tu 0 den 10."); return false;
        }
        return true;
    }

    private void them() {
        if (!kiemTra()) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("INSERT INTO ket_qua(ma_sv,mon_hoc,diem) VALUES(?,?,?)")) {
            p.setString(1, txtMaSV.getText().trim()); p.setString(2, txtMon.getText().trim());
            p.setBigDecimal(3, new java.math.BigDecimal(txtDiem.getText().trim())); p.executeUpdate();
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void sua() {
        if (txtMa.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chon diem can sua."); return; }
        if (!kiemTra()) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE ket_qua SET ma_sv=?,mon_hoc=?,diem=? WHERE ma_kq=?")) {
            p.setString(1, txtMaSV.getText().trim()); p.setString(2, txtMon.getText().trim());
            p.setBigDecimal(3, new java.math.BigDecimal(txtDiem.getText().trim()));
            p.setInt(4, Integer.parseInt(txtMa.getText())); p.executeUpdate();
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void xoa() {
        if (txtMa.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Chon diem can xoa."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Xoa ket qua nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM ket_qua WHERE ma_kq=?")) {
            p.setInt(1, Integer.parseInt(txtMa.getText())); p.executeUpdate();
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void lamMoi() {
        txtMa.setText(""); txtMaSV.setText(""); txtMon.setText(""); txtDiem.setText("");
        txtTim.setText(""); table.clearSelection();
    }

    private void baoLoi(Exception ex) {
        JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Thong bao", JOptionPane.ERROR_MESSAGE);
    }
}
