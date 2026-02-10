-- 1. Buat Database
CREATE DATABASE pegawai2;
USE pegawai2;

-- ==========================================
-- TABEL MASTER 1: JABATAN
-- Menyimpan data posisi dan gaji pokok
-- ==========================================
CREATE TABLE jabatan (
    id_jabatan INT AUTO_INCREMENT PRIMARY KEY,
    nama_jabatan VARCHAR(50) NOT NULL,
    gaji_pokok DOUBLE NOT NULL,
    tunjangan DOUBLE NOT NULL
);

-- Isi data dummy untuk Jabatan
INSERT INTO jabatan (nama_jabatan, gaji_pokok, tunjangan) VALUES 
('Manager', 8000000, 2000000),
('Supervisor', 6000000, 1500000),
('Staff IT', 5000000, 1000000),
('Admin', 4000000, 800000);

-- ==========================================
-- TABEL MASTER 2: KARYAWAN
-- Menyimpan data diri pegawai
-- ==========================================
CREATE TABLE karyawan (
    id_karyawan INT AUTO_INCREMENT PRIMARY KEY,
    id_jabatan INT, -- Foreign Key
    nama_karyawan VARCHAR(100) NOT NULL,
    jenis_kelamin ENUM('L', 'P') NOT NULL,
    no_telepon VARCHAR(15),
    alamat TEXT,
    FOREIGN KEY (id_jabatan) REFERENCES jabatan(id_jabatan)
);

-- Isi data dummy untuk Karyawan
INSERT INTO karyawan (id_jabatan, nama_karyawan, jenis_kelamin, no_telepon, alamat) VALUES 
(1, 'Budi Santoso', 'L', '08123456789', 'Jl. Merdeka No. 1'),
(3, 'Siti Aminah', 'P', '08987654321', 'Jl. Sudirman No. 45'),
(3, 'Rudi Hartono', 'L', '0811223344', 'Jl. Gatot Subroto No. 10');

-- ==========================================
-- TABEL TRANSAKSI: PENGGAJIAN
-- Menyimpan riwayat pembayaran gaji (transaksi)
-- ==========================================
CREATE TABLE penggajian (
    id_penggajian INT AUTO_INCREMENT PRIMARY KEY,
    id_karyawan INT, -- Foreign Key
    tanggal_gaji DATE NOT NULL,
    potongan DOUBLE DEFAULT 0,
    total_gaji DOUBLE NOT NULL, -- (Gaji Pokok + Tunjangan - Potongan)
    keterangan VARCHAR(100),
    FOREIGN KEY (id_karyawan) REFERENCES karyawan(id_karyawan)
);

-- Isi data dummy Transaksi Penggajian
INSERT INTO penggajian (id_karyawan, tanggal_gaji, potongan, total_gaji, keterangan) VALUES 
(1, '2024-01-25', 0, 10000000, 'Gaji Januari 2024'),
(2, '2024-01-25', 50000, 5950000, 'Gaji Januari 2024 - Telat 1x'),
(3, '2024-01-25', 0, 6000000, 'Gaji Januari 2024');