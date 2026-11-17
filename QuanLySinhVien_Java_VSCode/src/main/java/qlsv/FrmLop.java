package qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FrmLop extends JPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtKhoa = new JTextField();
    private final JTextField txtTim = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Ma lop", "Ten lop", "Khoa"}, 0);
    private final JTable table = new JTable(model);

    public FrmLop() {
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(2, 3, 8, 5));
        form.add(new JLabel("Ma lop")); form.add(new JLabel("Ten lop")); form.add(new JLabel("Khoa"));
        form.add(txtMa); form.add(txtTen); form.add(txtKhoa);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Them"), update = new JButton("Sua");
        JButton delete = new JButton("Xoa"), clear = new JButton("Lam moi");
        JButton search = new JButton("Tim kiem");
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(clear);
        buttons.add(new JLabel("Tim ten lop:")); buttons.add(txtTim); buttons.add(search);
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
                txtTen.setText(model.getValueAt(r, 1).toString());
                txtKhoa.setText(model.getValueAt(r, 2) == null ? "" : model.getValueAt(r, 2).toString());
            }
        });
        taiDuLieu("");
    }

    private void taiDuLieu(String key) {
        model.setRowCount(0);
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT * FROM lop WHERE ten_lop LIKE ? OR ma_lop LIKE ? ORDER BY ma_lop")) {
            p.setString(1, "%" + key + "%"); p.setString(2, "%" + key + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) model.addRow(new Object[]{rs.getString("ma_lop"), rs.getString("ten_lop"), rs.getString("khoa")});
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private boolean kiemTra() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ma lop va ten lop."); return false;
        }
        return true;
    }

    private void them() {
        if (!kiemTra()) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("INSERT INTO lop(ma_lop,ten_lop,khoa) VALUES(?,?,?)")) {
            p.setString(1, txtMa.getText().trim()); p.setString(2, txtTen.getText().trim());
            p.setString(3, txtKhoa.getText().trim()); p.executeUpdate();
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void sua() {
        if (!kiemTra()) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("UPDATE lop SET ten_lop=?,khoa=? WHERE ma_lop=?")) {
            p.setString(1, txtTen.getText().trim()); p.setString(2, txtKhoa.getText().trim());
            p.setString(3, txtMa.getText().trim());
            if (p.executeUpdate() == 0) JOptionPane.showMessageDialog(this, "Khong tim thay lop.");
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void xoa() {
        if (txtMa.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Chon lop can xoa."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Xoa lop nay?", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM lop WHERE ma_lop=?")) {
            p.setString(1, txtMa.getText().trim()); p.executeUpdate();
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void lamMoi() {
        txtMa.setText(""); txtTen.setText(""); txtKhoa.setText(""); txtTim.setText(""); table.clearSelection();
    }

    private void baoLoi(Exception ex) {
        JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Thong bao", JOptionPane.ERROR_MESSAGE);
    }
}
