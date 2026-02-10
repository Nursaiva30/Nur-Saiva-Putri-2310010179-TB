/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package form;

import util.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

/**
 * Panel Form untuk mengelola data Jabatan
 * @author Saputra
 */
public class JabatanPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private int selectedId = -1;

    /**
     * Creates new form JabatanPanel
     */
    public JabatanPanel() {
        initComponents();
        setupTable();
        loadData();
    }

    private void setupTable() {
        String[] columns = {"ID", "Nama Jabatan", "Gaji Pokok", "Tunjangan"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblJabatan.setModel(tableModel);
        tblJabatan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        tblJabatan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblJabatan.getSelectedRow() != -1) {
                int row = tblJabatan.getSelectedRow();
                selectedId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                txtNamaJabatan.setText(tableModel.getValueAt(row, 1).toString());
                txtGajiPokok.setText(tableModel.getValueAt(row, 2).toString());
                txtTunjangan.setText(tableModel.getValueAt(row, 3).toString());
            }
        });
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
                    rs.getDouble("gaji_pokok"),
                    rs.getDouble("tunjangan")
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
        tblJabatan.clearSelection();
    }
    
    private void searchData() {
    tableModel.setRowCount(0);
    String keyword = txtSearch.getText().trim();

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(
             "SELECT * FROM jabatan WHERE nama_jabatan LIKE ? ORDER BY id_jabatan")) {

        pst.setString(1, "%" + keyword + "%");
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Object[] row = {
                rs.getInt("id_jabatan"),
                rs.getString("nama_jabatan"),
                rs.getDouble("gaji_pokok"),
                rs.getDouble("tunjangan")
            };
            tableModel.addRow(row);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error pencarian: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        pnlForm = new javax.swing.JPanel();
        lblNamaJabatan = new javax.swing.JLabel();
        txtNamaJabatan = new javax.swing.JTextField();
        lblGajiPokok = new javax.swing.JLabel();
        txtGajiPokok = new javax.swing.JTextField();
        lblTunjangan = new javax.swing.JLabel();
        txtTunjangan = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblJabatan = new javax.swing.JTable();

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Data Jabatan");

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Input Data"));

        lblNamaJabatan.setText("Nama Jabatan:");

        lblGajiPokok.setText("Gaji Pokok:");

        lblTunjangan.setText("Tunjangan:");

        btnSimpan.setText("Simpan");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        btnUpdate.setText("Update");
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch.setText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFormLayout = new javax.swing.GroupLayout(pnlForm);
        pnlForm.setLayout(pnlFormLayout);
        pnlFormLayout.setHorizontalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNamaJabatan)
                            .addComponent(lblGajiPokok)
                            .addComponent(lblTunjangan))
                        .addGap(18, 18, 18)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNamaJabatan)
                            .addComponent(txtGajiPokok)
                            .addComponent(txtTunjangan)))
                    .addGroup(pnlFormLayout.createSequentialGroup()
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtSearch, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(btnSimpan)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnUpdate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnHapus)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnClear)
                            .addComponent(btnSearch))
                        .addGap(0, 10, Short.MAX_VALUE)))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNamaJabatan)
                    .addComponent(txtNamaJabatan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGajiPokok)
                    .addComponent(txtGajiPokok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTunjangan)
                    .addComponent(txtTunjangan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnUpdate)
                    .addComponent(btnHapus)
                    .addComponent(btnClear))
                .addGap(26, 26, 26)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblJabatan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Nama Jabatan", "Gaji Pokok", "Tunjangan"
            }
        ));
        jScrollPane1.setViewportView(tblJabatan);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlForm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        simpanData();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        updateData();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        hapusData();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        txtSearch.setText("");
        loadData();
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
       searchData();
    }//GEN-LAST:event_btnSearchActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblGajiPokok;
    private javax.swing.JLabel lblNamaJabatan;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTunjangan;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JTable tblJabatan;
    private javax.swing.JTextField txtGajiPokok;
    private javax.swing.JTextField txtNamaJabatan;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTunjangan;
    // End of variables declaration//GEN-END:variables
}
