package form;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Form Transaksi Penggajian
 */
public class PenggajianForm extends JPanel {
    
    private JComboBox<String> cmbKaryawan;
    private JTextField txtPotongan, txtTotalGaji, txtKeterangan, txtTanggal;
    private JTextField txtGajiPokok, txtTunjangan;
    private JTable table;
    private DefaultTableModel tableModel;
    private int selectedId = -1;
    private Map<String, Integer> karyawanMap = new HashMap<>();
    private Map<String, double[]> karyawanGajiMap = new HashMap<>();
    
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public PenggajianForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel lblTitle = new JLabel("Transaksi Penggajian", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);
        
        // Main split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(320);
        
        // Form Panel
        JPanel formPanel = createFormPanel();
        splitPane.setLeftComponent(formPanel);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        loadKaryawan();
        loadData();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Input Transaksi"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Karyawan
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Karyawan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbKaryawan = new JComboBox<>();
        cmbKaryawan.addActionListener(e -> updateGajiInfo());
        panel.add(cmbKaryawan, gbc);
        
        // Info Gaji Panel
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        JPanel gajiInfoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        gajiInfoPanel.setBorder(BorderFactory.createTitledBorder("Info Gaji"));
        
        gajiInfoPanel.add(new JLabel("Gaji Pokok:"));
        txtGajiPokok = new JTextField(10);
        txtGajiPokok.setEditable(false);
        gajiInfoPanel.add(txtGajiPokok);
        
        gajiInfoPanel.add(new JLabel("Tunjangan:"));
        txtTunjangan = new JTextField(10);
        txtTunjangan.setEditable(false);
        gajiInfoPanel.add(txtTunjangan);
        
        panel.add(gajiInfoPanel, gbc);
        
        // Tanggal Gaji
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tanggal (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTanggal = new JTextField(15);
        txtTanggal.setText(dateFormat.format(new java.util.Date()));
        panel.add(txtTanggal, gbc);
        
        // Potongan
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Potongan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtPotongan = new JTextField(15);
        txtPotongan.setText("0");
        txtPotongan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                hitungTotalGaji();
            }
        });
        panel.add(txtPotongan, gbc);
        
        // Total Gaji
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Total Gaji:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTotalGaji = new JTextField(15);
        txtTotalGaji.setEditable(false);
        txtTotalGaji.setFont(new Font("Dialog", Font.BOLD, 12));
        panel.add(txtTotalGaji, gbc);
        
        // Keterangan
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Keterangan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtKeterangan = new JTextField(15);
        panel.add(txtKeterangan, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        JButton btnSimpan = new JButton("Simpan");
        JButton btnUpdate = new JButton("Update");
        JButton btnHapus = new JButton("Hapus");
        JButton btnClear = new JButton("Clear");
        
        btnSimpan.addActionListener(e -> simpanData());
        btnUpdate.addActionListener(e -> updateData());
        btnHapus.addActionListener(e -> hapusData());
        btnClear.addActionListener(e -> clearForm());
        
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnHapus);
        buttonPanel.add(btnClear);
        
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Riwayat Penggajian"));
        
        String[] columns = {"ID", "Nama Karyawan", "Tanggal", "Gaji Pokok", "Tunjangan", "Potongan", "Total Gaji", "Keterangan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                String namaKaryawan = tableModel.getValueAt(row, 1).toString();
                cmbKaryawan.setSelectedItem(namaKaryawan);
                
                txtTanggal.setText(tableModel.getValueAt(row, 2).toString());
                
                String potongan = tableModel.getValueAt(row, 5).toString().replaceAll("[^0-9]", "");
                txtPotongan.setText(potongan);
                txtKeterangan.setText(tableModel.getValueAt(row, 7) != null ? 
                    tableModel.getValueAt(row, 7).toString() : "");
                
                hitungTotalGaji();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadKaryawan() {
        cmbKaryawan.removeAllItems();
        karyawanMap.clear();
        karyawanGajiMap.clear();
        
        String query = """
            SELECT k.id_karyawan, k.nama_karyawan, j.gaji_pokok, j.tunjangan
            FROM karyawan k
            LEFT JOIN jabatan j ON k.id_jabatan = j.id_jabatan
            ORDER BY k.nama_karyawan
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String nama = rs.getString("nama_karyawan");
                int id = rs.getInt("id_karyawan");
                double gajiPokok = rs.getDouble("gaji_pokok");
                double tunjangan = rs.getDouble("tunjangan");
                
                cmbKaryawan.addItem(nama);
                karyawanMap.put(nama, id);
                karyawanGajiMap.put(nama, new double[]{gajiPokok, tunjangan});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading karyawan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateGajiInfo() {
        String selectedKaryawan = (String) cmbKaryawan.getSelectedItem();
        if (selectedKaryawan != null && karyawanGajiMap.containsKey(selectedKaryawan)) {
            double[] gaji = karyawanGajiMap.get(selectedKaryawan);
            txtGajiPokok.setText(currencyFormat.format(gaji[0]));
            txtTunjangan.setText(currencyFormat.format(gaji[1]));
            hitungTotalGaji();
        }
    }
    
    private void hitungTotalGaji() {
        String selectedKaryawan = (String) cmbKaryawan.getSelectedItem();
        if (selectedKaryawan != null && karyawanGajiMap.containsKey(selectedKaryawan)) {
            double[] gaji = karyawanGajiMap.get(selectedKaryawan);
            double potongan = 0;
            try {
                potongan = Double.parseDouble(txtPotongan.getText().trim());
            } catch (NumberFormatException e) {}
            
            double totalGaji = gaji[0] + gaji[1] - potongan;
            txtTotalGaji.setText(currencyFormat.format(totalGaji));
        }
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        String query = """
            SELECT p.id_penggajian, k.nama_karyawan, p.tanggal_gaji, 
                   j.gaji_pokok, j.tunjangan, p.potongan, p.total_gaji, p.keterangan
            FROM penggajian p
            JOIN karyawan k ON p.id_karyawan = k.id_karyawan
            LEFT JOIN jabatan j ON k.id_jabatan = j.id_jabatan
            ORDER BY p.tanggal_gaji DESC, p.id_penggajian DESC
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_penggajian"),
                    rs.getString("nama_karyawan"),
                    rs.getDate("tanggal_gaji").toString(),
                    currencyFormat.format(rs.getDouble("gaji_pokok")),
                    currencyFormat.format(rs.getDouble("tunjangan")),
                    currencyFormat.format(rs.getDouble("potongan")),
                    currencyFormat.format(rs.getDouble("total_gaji")),
                    rs.getString("keterangan")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void simpanData() {
        if (!validateForm()) return;
        
        String selectedKaryawan = (String) cmbKaryawan.getSelectedItem();
        double[] gaji = karyawanGajiMap.get(selectedKaryawan);
        double potongan = Double.parseDouble(txtPotongan.getText().trim());
        double totalGaji = gaji[0] + gaji[1] - potongan;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO penggajian (id_karyawan, tanggal_gaji, potongan, total_gaji, keterangan) VALUES (?, ?, ?, ?, ?)")) {
            
            pstmt.setInt(1, karyawanMap.get(selectedKaryawan));
            pstmt.setDate(2, java.sql.Date.valueOf(txtTanggal.getText().trim()));
            pstmt.setDouble(3, potongan);
            pstmt.setDouble(4, totalGaji);
            pstmt.setString(5, txtKeterangan.getText().trim());
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Transaksi penggajian berhasil disimpan!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah! Gunakan format YYYY-MM-DD", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateData() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan diupdate!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) return;
        
        String selectedKaryawan = (String) cmbKaryawan.getSelectedItem();
        double[] gaji = karyawanGajiMap.get(selectedKaryawan);
        double potongan = Double.parseDouble(txtPotongan.getText().trim());
        double totalGaji = gaji[0] + gaji[1] - potongan;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE penggajian SET id_karyawan=?, tanggal_gaji=?, potongan=?, total_gaji=?, keterangan=? WHERE id_penggajian=?")) {
            
            pstmt.setInt(1, karyawanMap.get(selectedKaryawan));
            pstmt.setDate(2, java.sql.Date.valueOf(txtTanggal.getText().trim()));
            pstmt.setDouble(3, potongan);
            pstmt.setDouble(4, totalGaji);
            pstmt.setString(5, txtKeterangan.getText().trim());
            pstmt.setInt(6, selectedId);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah! Gunakan format YYYY-MM-DD", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void hapusData() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data yang akan dihapus!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Yakin ingin menghapus transaksi ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM penggajian WHERE id_penggajian=?")) {
                
                pstmt.setInt(1, selectedId);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Transaksi berhasil dihapus!");
                clearForm();
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        if (cmbKaryawan.getItemCount() > 0) cmbKaryawan.setSelectedIndex(0);
        txtPotongan.setText("0");
        txtKeterangan.setText("");
        txtTanggal.setText(dateFormat.format(new java.util.Date()));
        selectedId = -1;
        table.clearSelection();
        hitungTotalGaji();
    }
    
    private boolean validateForm() {
        if (cmbKaryawan.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Karyawan harus dipilih!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (txtTanggal.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tanggal gaji harus diisi!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            java.sql.Date.valueOf(txtTanggal.getText().trim());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah! Gunakan format YYYY-MM-DD", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(txtPotongan.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Potongan harus berupa angka!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    public void refreshKaryawan() {
        loadKaryawan();
    }
}
