package qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class FrmSinhVien extends JPanel {
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtNgaySinh = new JTextField();
    private final JComboBox<String> cboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nu", "Khac"});
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtSDT = new JTextField();
    private final JTextField txtMaLop = new JTextField();
    private final JTextField txtTim = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Ma SV", "Ho ten", "Ngay sinh", "Gioi tinh", "Email", "Dien thoai", "Ma lop"}, 0);
    private final JTable table = new JTable(model);

    public FrmSinhVien() {
        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(2, 7, 6, 5));
        String[] labels = {"Ma sinh vien", "Ho ten", "Ngay sinh (yyyy-mm-dd)", "Gioi tinh", "Email", "So dien thoai", "Ma lop"};
        for (String s : labels) form.add(new JLabel(s));
        form.add(txtMa); form.add(txtTen); form.add(txtNgaySinh); form.add(cboGioiTinh);
        form.add(txtEmail); form.add(txtSDT); form.add(txtMaLop);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Them");
        JButton update = new JButton("Sua");
        JButton delete = new JButton("Xoa");
        JButton clear = new JButton("Lam moi");
        JButton search = new JButton("Tim kiem");
        buttons.add(add); buttons.add(update); buttons.add(delete); buttons.add(clear);
        buttons.add(new JLabel("Tim theo ten/ma:")); buttons.add(txtTim); buttons.add(search);

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(form, BorderLayout.NORTH);
        top.add(buttons, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> them());
        update.addActionListener(e -> sua());
        delete.addActionListener(e -> xoa());
        clear.addActionListener(e -> lamMoi());
        search.addActionListener(e -> taiDuLieu(txtTim.getText().trim()));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int r = table.getSelectedRow();
                txtMa.setText(value(r, 0));
                txtTen.setText(value(r, 1));
                txtNgaySinh.setText(value(r, 2));
                cboGioiTinh.setSelectedItem(value(r, 3));
                txtEmail.setText(value(r, 4));
                txtSDT.setText(value(r, 5));
                txtMaLop.setText(value(r, 6));
            }
        });
        taiDuLieu("");
    }

    private String value(int row, int col) {
        Object v = model.getValueAt(row, col);
        return v == null ? "" : v.toString();
    }

    private void taiDuLieu(String tuKhoa) {
        model.setRowCount(0);
        String sql = "SELECT * FROM sinh_vien WHERE ma_sv LIKE ? OR ho_ten LIKE ? ORDER BY ma_sv";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + tuKhoa + "%");
            p.setString(2, "%" + tuKhoa + "%");
            ResultSet rs = p.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getString("ma_sv"), rs.getString("ho_ten"),
                        rs.getDate("ngay_sinh"), rs.getString("gioi_tinh"), rs.getString("email"),
                        rs.getString("so_dien_thoai"), rs.getString("ma_lop")});
            }
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private boolean kiemTra() {
        if (txtMa.getText().trim().isEmpty() || txtTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap ma sinh vien va ho ten.");
            return false;
        }
        if (!txtNgaySinh.getText().trim().isEmpty()) {
            try { Date.valueOf(txtNgaySinh.getText().trim()); }
            catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Ngay sinh phai theo dang yyyy-mm-dd.");
                return false;
            }
        }
        return true;
    }

    private void them() {
        if (!kiemTra()) return;
        String sql = "INSERT INTO sinh_vien(ma_sv,ho_ten,ngay_sinh,gioi_tinh,email,so_dien_thoai,ma_lop) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            dienThongTin(p, false);
            p.executeUpdate();
            taiDuLieu(""); lamMoi();
            JOptionPane.showMessageDialog(this, "Them sinh vien thanh cong.");
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void sua() {
        if (!kiemTra()) return;
        String sql = "UPDATE sinh_vien SET ho_ten=?,ngay_sinh=?,gioi_tinh=?,email=?,so_dien_thoai=?,ma_lop=? WHERE ma_sv=?";
        try (Connection c = DB.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, txtTen.getText().trim());
            setDate(p, 2);
            p.setString(3, cboGioiTinh.getSelectedItem().toString());
            p.setString(4, txtEmail.getText().trim());
            p.setString(5, txtSDT.getText().trim());
            setMaLop(p, 6);
            p.setString(7, txtMa.getText().trim());
            if (p.executeUpdate() == 0) JOptionPane.showMessageDialog(this, "Khong tim thay sinh vien de sua.");
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void dienThongTin(PreparedStatement p, boolean coMa) throws SQLException {
        int i = 1;
        p.setString(i++, txtMa.getText().trim());
        p.setString(i++, txtTen.getText().trim());
        setDate(p, i++);
        p.setString(i++, cboGioiTinh.getSelectedItem().toString());
        p.setString(i++, txtEmail.getText().trim());
        p.setString(i++, txtSDT.getText().trim());
        setMaLop(p, i);
    }

    private void setDate(PreparedStatement p, int index) throws SQLException {
        String s = txtNgaySinh.getText().trim();
        if (s.isEmpty()) p.setNull(index, Types.DATE);
        else p.setDate(index, Date.valueOf(s));
    }

    private void setMaLop(PreparedStatement p, int index) throws SQLException {
        String s = txtMaLop.getText().trim();
        if (s.isEmpty()) p.setNull(index, Types.VARCHAR);
        else p.setString(index, s);
    }

    private void xoa() {
        if (txtMa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhap/chon ma sinh vien can xoa."); return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Ban chac chan muon xoa sinh vien nay?",
                "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try (Connection c = DB.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM sinh_vien WHERE ma_sv=?")) {
            p.setString(1, txtMa.getText().trim());
            p.executeUpdate();
            taiDuLieu(""); lamMoi();
        } catch (SQLException ex) { baoLoi(ex); }
    }

    private void lamMoi() {
        txtMa.setText(""); txtTen.setText(""); txtNgaySinh.setText("");
        cboGioiTinh.setSelectedIndex(0); txtEmail.setText(""); txtSDT.setText("");
        txtMaLop.setText(""); txtTim.setText(""); table.clearSelection();
    }

    private void baoLoi(Exception ex) {
        JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Thong bao", JOptionPane.ERROR_MESSAGE);
    }
}
