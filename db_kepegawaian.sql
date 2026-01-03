-- phpMyAdmin SQL Dump
-- version 6.0.0-dev+20250909.be01432c56
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jan 01, 2026 at 12:14 PM
-- Server version: 8.4.3
-- PHP Version: 8.4.5

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_kepegawaian`
--
CREATE DATABASE IF NOT EXISTS `db_kepegawaian` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `db_kepegawaian`;

-- --------------------------------------------------------

--
-- Table structure for table `jabatan`
--

CREATE TABLE IF NOT EXISTS `jabatan` (
  `id_jabatan` int NOT NULL AUTO_INCREMENT,
  `nama_jabatan` varchar(50) NOT NULL,
  `gaji_pokok` double NOT NULL,
  `tunjangan` double NOT NULL,
  PRIMARY KEY (`id_jabatan`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `jabatan`
--

INSERT INTO `jabatan` (`id_jabatan`, `nama_jabatan`, `gaji_pokok`, `tunjangan`) VALUES
(1, 'Manager', 8000000, 2000000),
(2, 'Supervisor', 6000000, 1500000),
(3, 'Staff IT', 5000000, 1000000),
(4, 'Admin', 4000000, 800000);

-- --------------------------------------------------------

--
-- Table structure for table `karyawan`
--

CREATE TABLE IF NOT EXISTS `karyawan` (
  `id_karyawan` int NOT NULL AUTO_INCREMENT,
  `id_jabatan` int DEFAULT NULL,
  `nama_karyawan` varchar(100) NOT NULL,
  `jenis_kelamin` enum('L','P') NOT NULL,
  `no_telepon` varchar(15) DEFAULT NULL,
  `alamat` text,
  PRIMARY KEY (`id_karyawan`),
  KEY `id_jabatan` (`id_jabatan`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `karyawan`
--

INSERT INTO `karyawan` (`id_karyawan`, `id_jabatan`, `nama_karyawan`, `jenis_kelamin`, `no_telepon`, `alamat`) VALUES
(1, 1, 'Budi Santoso', 'L', '08123456789', 'Jl. Merdeka No. 1'),
(2, 3, 'Siti Aminah', 'P', '08987654321', 'Jl. Sudirman No. 45'),
(3, 3, 'Rudi Hartono', 'L', '0811223344', 'Jl. Gatot Subroto No. 10');

-- --------------------------------------------------------

--
-- Table structure for table `penggajian`
--

CREATE TABLE IF NOT EXISTS `penggajian` (
  `id_penggajian` int NOT NULL AUTO_INCREMENT,
  `id_karyawan` int DEFAULT NULL,
  `tanggal_gaji` date NOT NULL,
  `potongan` double DEFAULT '0',
  `total_gaji` double NOT NULL,
  `keterangan` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_penggajian`),
  KEY `id_karyawan` (`id_karyawan`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `penggajian`
--

INSERT INTO `penggajian` (`id_penggajian`, `id_karyawan`, `tanggal_gaji`, `potongan`, `total_gaji`, `keterangan`) VALUES
(1, 1, '2024-01-25', 0, 10000000, 'Gaji Januari 2024'),
(2, 2, '2024-01-25', 50000, 5950000, 'Gaji Januari 2024 - Telat 1x'),
(3, 3, '2024-01-25', 0, 6000000, 'Gaji Januari 2024');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `karyawan`
--
ALTER TABLE `karyawan`
  ADD CONSTRAINT `karyawan_ibfk_1` FOREIGN KEY (`id_jabatan`) REFERENCES `jabatan` (`id_jabatan`);

--
-- Constraints for table `penggajian`
--
ALTER TABLE `penggajian`
  ADD CONSTRAINT `penggajian_ibfk_1` FOREIGN KEY (`id_karyawan`) REFERENCES `karyawan` (`id_karyawan`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
