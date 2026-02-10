/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package form;

import util.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;

/**
 * Panel Form untuk mengelola transaksi Penggajian
 * @author Saputra
 */
public class PenggajianPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private Map<String, Integer> karyawanMap = new HashMap<>();
    private Map<String, double[]> gajiMap = new HashMap<>();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Creates new form PenggajianPanel
     */
    public PenggajianPanel() {
        initComponents();
        setupTable();
        loadKaryawan();
        loadData();
    }

    private void setupTable() {
        String[] columns = {"ID", "Tanggal", "Karyawan", "Jabatan", "Total Gaji"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblPenggajian.setModel(tableModel);
        tblPenggajian.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void loadKaryawan() {
        cmbKaryawan.removeAllItems();
        karyawanMap.clear();
        gajiMap.clear();
        
        String query = "SELECT k.id_karyawan, k.nama_karyawan, j.nama_jabatan, j.gaji_pokok, j.tunjangan " +
                      "FROM karyawan k JOIN jabatan j ON k.id_jabatan = j.id_jabatan ORDER BY k.nama_karyawan";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                String namaKaryawan = rs.getString("nama_karyawan");
                int idKaryawan = rs.getInt("id_karyawan");
                String namaJabatan = rs.getString("nama_jabatan");
                double gajiPokok = rs.getDouble("gaji_pokok");
                double tunjangan = rs.getDouble("tunjangan");
                
                String displayName = namaKaryawan + " - " + namaJabatan;
                cmbKaryawan.addItem(displayName);
                karyawanMap.put(displayName, idKaryawan);
                gajiMap.put(displayName, new double[]{gajiPokok, tunjangan});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading karyawan: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        updateGajiDisplay();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String query = "SELECT p.id_penggajian, p.tanggal_gaji, k.nama_karyawan, j.nama_jabatan, " +
                      "p.total_gaji " +
                      "FROM penggajian p " +
                      "JOIN karyawan k ON p.id_karyawan = k.id_karyawan " +
                      "JOIN jabatan j ON k.id_jabatan = j.id_jabatan " +
                      "ORDER BY p.tanggal_gaji DESC, p.id_penggajian DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id_penggajian"),
                    rs.getDate("tanggal_gaji"),
                    rs.getString("nama_karyawan"),
                    rs.getString("nama_jabatan"),
                    currencyFormat.format(rs.getDouble("total_gaji"))
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGajiDisplay() {
        String selected = (String) cmbKaryawan.getSelectedItem();
        if (selected != null && gajiMap.containsKey(selected)) {
            double[] gaji = gajiMap.get(selected);
            txtGajiPokok.setText(currencyFormat.format(gaji[0]));
            txtTunjangan.setText(currencyFormat.format(gaji[1]));
            txtTotalGaji.setText(currencyFormat.format(gaji[0] + gaji[1]));
        } else {
            txtGajiPokok.setText("");
            txtTunjangan.setText("");
            txtTotalGaji.setText("");
        }
    }

    private void prosesGaji() {
        String selected = (String) cmbKaryawan.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih karyawan terlebih dahulu!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!karyawanMap.containsKey(selected) || !gajiMap.containsKey(selected)) {
            JOptionPane.showMessageDialog(this, "Data karyawan tidak valid, silakan refresh data.", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Proses penggajian untuk " + selected + "?", 
            "Konfirmasi", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            double[] gaji = gajiMap.get(selected);
            double totalGaji = gaji[0] + gaji[1];
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO penggajian (id_karyawan, tanggal_gaji, potongan, total_gaji, keterangan) " +
                     "VALUES (?, CURDATE(), ?, ?, ?)")) {
                
                pstmt.setInt(1, karyawanMap.get(selected));
                pstmt.setDouble(2, 0);
                pstmt.setDouble(3, totalGaji);
                pstmt.setString(4, "Gaji diproses via aplikasi");
                pstmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Penggajian berhasil diproses!");
                loadData();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshKaryawan() {
        loadKaryawan();
    }
    private void searchData() {
    tableModel.setRowCount(0);
    String keyword = txtSearch.getText().trim();

    String query = "SELECT p.id_penggajian, p.tanggal_gaji, k.nama_karyawan, " +
                   "j.nama_jabatan, p.total_gaji " +
                   "FROM penggajian p " +
                   "JOIN karyawan k ON p.id_karyawan = k.id_karyawan " +
                   "JOIN jabatan j ON k.id_jabatan = j.id_jabatan " +
                   "WHERE k.nama_karyawan LIKE ? " +
                   "   OR j.nama_jabatan LIKE ? " +
                   "ORDER BY p.tanggal_gaji DESC, p.id_penggajian DESC";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pst = conn.prepareStatement(query)) {

        pst.setString(1, "%" + keyword + "%");
        pst.setString(2, "%" + keyword + "%");

        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            Object[] row = {
                rs.getInt("id_penggajian"),
                rs.getDate("tanggal_gaji"),
                rs.getString("nama_karyawan"),
                rs.getString("nama_jabatan"),
                currencyFormat.format(rs.getDouble("total_gaji"))
            };
            tableModel.addRow(row);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error pencarian: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
    }
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
        lblKaryawan = new javax.swing.JLabel();
        cmbKaryawan = new javax.swing.JComboBox<>();
        lblGajiPokok = new javax.swing.JLabel();
        txtGajiPokok = new javax.swing.JTextField();
        lblTunjangan = new javax.swing.JLabel();
        txtTunjangan = new javax.swing.JTextField();
        lblTotalGaji = new javax.swing.JLabel();
        txtTotalGaji = new javax.swing.JTextField();
        btnProses = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPenggajian = new javax.swing.JTable();

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Transaksi Penggajian");

        pnlForm.setBorder(javax.swing.BorderFactory.createTitledBorder("Proses Gaji"));

        lblKaryawan.setText("Pilih Karyawan:");

        cmbKaryawan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKaryawanActionPerformed(evt);
            }
        });

        lblGajiPokok.setText("Gaji Pokok:");

        txtGajiPokok.setEditable(false);

        lblTunjangan.setText("Tunjangan:");

        txtTunjangan.setEditable(false);

        lblTotalGaji.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTotalGaji.setText("Total Gaji:");

        txtTotalGaji.setEditable(false);
        txtTotalGaji.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        btnProses.setBackground(new java.awt.Color(0, 153, 51));
        btnProses.setForeground(new java.awt.Color(255, 255, 255));
        btnProses.setText("Proses Gaji");
        btnProses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProsesActionPerformed(evt);
            }
        });

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
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
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblKaryawan)
                                    .addComponent(lblGajiPokok)
                                    .addComponent(lblTunjangan)
                                    .addComponent(lblTotalGaji))
                                .addGap(18, 18, 18)
                                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cmbKaryawan, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtGajiPokok)
                                    .addComponent(txtTunjangan)
                                    .addComponent(txtTotalGaji, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)))
                            .addGroup(pnlFormLayout.createSequentialGroup()
                                .addComponent(btnProses)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRefresh)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFormLayout.createSequentialGroup()
                        .addComponent(txtSearch)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearch)))
                .addContainerGap())
        );
        pnlFormLayout.setVerticalGroup(
            pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblKaryawan)
                    .addComponent(cmbKaryawan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblGajiPokok)
                    .addComponent(txtGajiPokok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTunjangan)
                    .addComponent(txtTunjangan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalGaji)
                    .addComponent(txtTotalGaji, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnProses)
                    .addComponent(btnRefresh))
                .addGap(18, 18, 18)
                .addGroup(pnlFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblPenggajian.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "Tanggal", "Karyawan", "Jabatan", "Total Gaji"
            }
        ));
        jScrollPane1.setViewportView(tblPenggajian);

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
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)))
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

    private void cmbKaryawanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKaryawanActionPerformed
        updateGajiDisplay();
    }//GEN-LAST:event_cmbKaryawanActionPerformed

    private void btnProsesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProsesActionPerformed
        prosesGaji();
    }//GEN-LAST:event_btnProsesActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        loadKaryawan();
        loadData();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        searchData();
    }//GEN-LAST:event_btnSearchActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnProses;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cmbKaryawan;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblGajiPokok;
    private javax.swing.JLabel lblKaryawan;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalGaji;
    private javax.swing.JLabel lblTunjangan;
    private javax.swing.JPanel pnlForm;
    private javax.swing.JTable tblPenggajian;
    private javax.swing.JTextField txtGajiPokok;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtTotalGaji;
    private javax.swing.JTextField txtTunjangan;
    // End of variables declaration//GEN-END:variables
}
