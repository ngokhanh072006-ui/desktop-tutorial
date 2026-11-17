CREATE DATABASE IF NOT EXISTS quanlysinhvien
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quanlysinhvien;

CREATE TABLE IF NOT EXISTS lop (
    ma_lop VARCHAR(20) PRIMARY KEY,
    ten_lop VARCHAR(100) NOT NULL,
    khoa VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS sinh_vien (
    ma_sv VARCHAR(20) PRIMARY KEY,
    ho_ten VARCHAR(120) NOT NULL,
    ngay_sinh DATE NULL,
    gioi_tinh VARCHAR(10),
    email VARCHAR(120),
    so_dien_thoai VARCHAR(20),
    ma_lop VARCHAR(20),
    CONSTRAINT fk_sv_lop FOREIGN KEY (ma_lop) REFERENCES lop(ma_lop)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ket_qua (
    ma_kq INT AUTO_INCREMENT PRIMARY KEY,
    ma_sv VARCHAR(20) NOT NULL,
    mon_hoc VARCHAR(100) NOT NULL,
    diem DECIMAL(4,2) NOT NULL,
    CONSTRAINT fk_kq_sv FOREIGN KEY (ma_sv) REFERENCES sinh_vien(ma_sv)
        ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT IGNORE INTO lop(ma_lop, ten_lop, khoa)
VALUES ('TH30.21', 'Công nghệ thông tin', 'Công nghệ');

INSERT IGNORE INTO sinh_vien(ma_sv, ho_ten, ngay_sinh, gioi_tinh, email, so_dien_thoai, ma_lop)
VALUES ('SV001', 'Nguyen Van An', '2005-01-15', 'Nam', 'an@example.com', '0900000001', 'TH30.21');
