package form;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Form CRUD untuk tabel Karyawan
 */
public class KaryawanForm extends JPanel {
    
    private JTextField txtNama, txtTelepon;
    private JTextArea txtAlamat;
    private JComboBox<String> cmbJabatan, cmbJenisKelamin;
    private JTable table;
    private DefaultTableModel tableModel;
    private int selectedId = -1;
    private Map<String, Integer> jabatanMap = new HashMap<>();
    
    public KaryawanForm() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title
        JLabel lblTitle = new JLabel("Data Karyawan", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);
        
        // Main split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(300);
        
        // Form Panel
        JPanel formPanel = createFormPanel();
        splitPane.setLeftComponent(formPanel);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        splitPane.setRightComponent(tablePanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        loadJabatan();
        loadData();
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Input Data"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Nama Karyawan
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Nama Karyawan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtNama = new JTextField(15);
        panel.add(txtNama, gbc);
        
        // Jenis Kelamin
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Jenis Kelamin:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbJenisKelamin = new JComboBox<>(new String[]{"Laki-laki", "Perempuan"});
        panel.add(cmbJenisKelamin, gbc);
        
        // Jabatan
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Jabatan:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbJabatan = new JComboBox<>();
        panel.add(cmbJabatan, gbc);
        
        // No Telepon
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("No. Telepon:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTelepon = new JTextField(15);
        panel.add(txtTelepon, gbc);
        
        // Alamat
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Alamat:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        txtAlamat = new JTextArea(3, 15);
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JScrollPane alamatScroll = new JScrollPane(txtAlamat);
        panel.add(alamatScroll, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5;
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
        panel.setBorder(BorderFactory.createTitledBorder("Daftar Karyawan"));
        
        String[] columns = {"ID", "Nama Karyawan", "JK", "Jabatan", "No. Telepon", "Alamat"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(30);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                txtNama.setText(tableModel.getValueAt(row, 1).toString());
                
                String jk = tableModel.getValueAt(row, 2).toString();
                cmbJenisKelamin.setSelectedItem(jk.equals("L") ? "Laki-laki" : "Perempuan");
                
                cmbJabatan.setSelectedItem(tableModel.getValueAt(row, 3).toString());
                txtTelepon.setText(tableModel.getValueAt(row, 4) != null ? 
                    tableModel.getValueAt(row, 4).toString() : "");
                txtAlamat.setText(tableModel.getValueAt(row, 5) != null ? 
                    tableModel.getValueAt(row, 5).toString() : "");
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadJabatan() {
        cmbJabatan.removeAllItems();
        jabatanMap.clear();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_jabatan, nama_jabatan FROM jabatan ORDER BY nama_jabatan")) {
            
            while (rs.next()) {
                String namaJabatan = rs.getString("nama_jabatan");
                int idJabatan = rs.getInt("id_jabatan");
                cmbJabatan.addItem(namaJabatan);
                jabatanMap.put(namaJabatan, idJabatan);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading jabatan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        String query = """
            SELECT k.id_karyawan, k.nama_karyawan, k.jenis_kelamin, 
                   j.nama_jabatan, k.no_telepon, k.alamat
            FROM karyawan k
            LEFT JOIN jabatan j ON k.id_jabatan = j.id_jabatan
            ORDER BY k.id_karyawan
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_karyawan"),
                    rs.getString("nama_karyawan"),
                    rs.getString("jenis_kelamin"),
                    rs.getString("nama_jabatan"),
                    rs.getString("no_telepon"),
                    rs.getString("alamat")
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
                 "INSERT INTO karyawan (id_jabatan, nama_karyawan, jenis_kelamin, no_telepon, alamat) VALUES (?, ?, ?, ?, ?)")) {
            
            String selectedJabatan = (String) cmbJabatan.getSelectedItem();
            pstmt.setInt(1, jabatanMap.get(selectedJabatan));
            pstmt.setString(2, txtNama.getText().trim());
            pstmt.setString(3, cmbJenisKelamin.getSelectedIndex() == 0 ? "L" : "P");
            pstmt.setString(4, txtTelepon.getText().trim());
            pstmt.setString(5, txtAlamat.getText().trim());
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
                 "UPDATE karyawan SET id_jabatan=?, nama_karyawan=?, jenis_kelamin=?, no_telepon=?, alamat=? WHERE id_karyawan=?")) {
            
            String selectedJabatan = (String) cmbJabatan.getSelectedItem();
            pstmt.setInt(1, jabatanMap.get(selectedJabatan));
            pstmt.setString(2, txtNama.getText().trim());
            pstmt.setString(3, cmbJenisKelamin.getSelectedIndex() == 0 ? "L" : "P");
            pstmt.setString(4, txtTelepon.getText().trim());
            pstmt.setString(5, txtAlamat.getText().trim());
            pstmt.setInt(6, selectedId);
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
                     "DELETE FROM karyawan WHERE id_karyawan=?")) {
                
                pstmt.setInt(1, selectedId);
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Data berhasil dihapus!");
                clearForm();
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage() + 
                    "\nPastikan tidak ada transaksi penggajian untuk karyawan ini.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        txtNama.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        if (cmbJenisKelamin.getItemCount() > 0) cmbJenisKelamin.setSelectedIndex(0);
        if (cmbJabatan.getItemCount() > 0) cmbJabatan.setSelectedIndex(0);
        selectedId = -1;
        table.clearSelection();
    }
    
    private boolean validateForm() {
        if (txtNama.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Karyawan harus diisi!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (cmbJabatan.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Jabatan harus dipilih!", 
                "Validasi", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    public void refreshJabatan() {
        loadJabatan();
    }
}
