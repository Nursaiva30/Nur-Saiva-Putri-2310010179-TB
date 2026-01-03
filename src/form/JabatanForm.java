package form;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Form CRUD untuk tabel Jabatan
 */
public class JabatanForm extends JPanel {
    
    private JTextField txtNamaJabatan, txtGajiPokok, txtTunjangan;
    private JTable table;
    private DefaultTableModel tableModel;
    private int selectedId = -1;
    
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    public JabatanForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel lblTitle = new JLabel("Data Jabatan", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);
        
        // Main split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(280);
        
        // Form Panel
        JPanel formPanel = createFormPanel();
        splitPane.setLeftComponent(formPanel);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        loadData();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Input Data"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nama Jabatan
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nama Jabatan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNamaJabatan = new JTextField(15);
        panel.add(txtNamaJabatan, gbc);
        
        // Gaji Pokok
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Gaji Pokok:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtGajiPokok = new JTextField(15);
        panel.add(txtGajiPokok, gbc);
        
        // Tunjangan
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Tunjangan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTunjangan = new JTextField(15);
        panel.add(txtTunjangan, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3;
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
        panel.setBorder(BorderFactory.createTitledBorder("Daftar Jabatan"));
        
        String[] columns = {"ID", "Nama Jabatan", "Gaji Pokok", "Tunjangan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                txtNamaJabatan.setText(tableModel.getValueAt(row, 1).toString());
                txtGajiPokok.setText(tableModel.getValueAt(row, 2).toString().replaceAll("[^0-9]", ""));
                txtTunjangan.setText(tableModel.getValueAt(row, 3).toString().replaceAll("[^0-9]", ""));
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM jabatan ORDER BY id_jabatan")) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_jabatan"),
                    rs.getString("nama_jabatan"),
                    currencyFormat.format(rs.getDouble("gaji_pokok")),
                    currencyFormat.format(rs.getDouble("tunjangan"))
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
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO jabatan (nama_jabatan, gaji_pokok, tunjangan) VALUES (?, ?, ?)")) {
            
            pstmt.setString(1, txtNamaJabatan.getText().trim());
            pstmt.setDouble(2, Double.parseDouble(txtGajiPokok.getText().trim()));
            pstmt.setDouble(3, Double.parseDouble(txtTunjangan.getText().trim()));
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
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
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE jabatan SET nama_jabatan=?, gaji_pokok=?, tunjangan=? WHERE id_jabatan=?")) {
            
            pstmt.setString(1, txtNamaJabatan.getText().trim());
            pstmt.setDouble(2, Double.parseDouble(txtGajiPokok.getText().trim()));
            pstmt.setDouble(3, Double.parseDouble(txtTunjangan.getText().trim()));
            pstmt.setInt(4, selectedId);
            pstmt.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Data berhasil diupdate!");
            clearForm();
            loadData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
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
            "Yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM jabatan WHERE id_jabatan=?")) {
                
                pstmt.setInt(1, selectedId);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                clearForm();
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage() + 
                    "\nPastikan tidak ada karyawan yang menggunakan jabatan ini.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        txtNamaJabatan.setText("");
        txtGajiPokok.setText("");
        txtTunjangan.setText("");
        selectedId = -1;
        table.clearSelection();
    }
    
    private boolean validateForm() {
        if (txtNamaJabatan.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Jabatan harus diisi!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        try {
            Double.parseDouble(txtGajiPokok.getText().trim());
            Double.parseDouble(txtTunjangan.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Gaji Pokok dan Tunjangan harus berupa angka!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
}
