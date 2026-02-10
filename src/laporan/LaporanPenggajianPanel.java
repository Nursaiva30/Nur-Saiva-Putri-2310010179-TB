/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package laporan;

import util.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Panel Laporan untuk menampilkan data Penggajian
 * @author 
 */
public class LaporanPenggajianPanel extends javax.swing.JPanel {

    private DefaultTableModel tableModel;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Creates new form LaporanPenggajianPanel
     */
    public LaporanPenggajianPanel() {
        initComponents();
        setupTable();
        setupDateSpinners();
        loadData();
    }

    private void setupTable() {
        String[] columns = {"ID", "Tanggal", "Nama Karyawan", "Jabatan", "Gaji Pokok", "Tunjangan", "Total Gaji"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblLaporan.setModel(tableModel);
    }

    private void setupDateSpinners() {
        // Set default dates - start of month to today
        java.util.Calendar cal = java.util.Calendar.getInstance();
        spnTanggalAkhir.setValue(cal.getTime());
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        spnTanggalAwal.setValue(cal.getTime());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        
        java.util.Date startDate = (java.util.Date) spnTanggalAwal.getValue();
        java.util.Date endDate = (java.util.Date) spnTanggalAkhir.getValue();
        
        String query = "SELECT p.id_penggajian, p.tanggal_gaji, k.nama_karyawan, " +
                      "j.nama_jabatan, j.gaji_pokok, j.tunjangan, " +
                      "p.total_gaji " +
                      "FROM penggajian p " +
                      "JOIN karyawan k ON p.id_karyawan = k.id_karyawan " +
                      "JOIN jabatan j ON k.id_jabatan = j.id_jabatan " +
                      "WHERE p.tanggal_gaji BETWEEN ? AND ? " +
                      "ORDER BY p.tanggal_gaji DESC";
        
        double totalGaji = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    double total = rs.getDouble("total_gaji");
                    totalGaji += total;
                    
                    Object[] row = {
                        rs.getInt("id_penggajian"),
                        rs.getDate("tanggal_gaji"),
                        rs.getString("nama_karyawan"),
                        rs.getString("nama_jabatan"),
                        currencyFormat.format(rs.getDouble("gaji_pokok")),
                        currencyFormat.format(rs.getDouble("tunjangan")),
                        currencyFormat.format(total)
                    };
                    tableModel.addRow(row);
                }
            }
            
            lblTotalData.setText("Total Data: " + tableModel.getRowCount() + " | Total Gaji: " + currencyFormat.format(totalGaji));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Penggajian");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new java.io.File("laporan_penggajian.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new java.io.File(file.getAbsolutePath() + ".csv");
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Write header
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    if (i > 0) writer.print(",");
                    writer.print("\"" + tableModel.getColumnName(i) + "\"");
                }
                writer.println();
                
                // Write data
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        if (col > 0) writer.print(",");
                        Object value = tableModel.getValueAt(row, col);
                        writer.print("\"" + (value != null ? value.toString() : "") + "\"");
                    }
                    writer.println();
                }
                
                JOptionPane.showMessageDialog(this, "Laporan berhasil diekspor ke:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
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
        pnlFilter = new javax.swing.JPanel();
        lblTanggalAwal = new javax.swing.JLabel();
        spnTanggalAwal = new javax.swing.JSpinner();
        lblTanggalAkhir = new javax.swing.JLabel();
        spnTanggalAkhir = new javax.swing.JSpinner();
        btnFilter = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        lblTotalData = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblLaporan = new javax.swing.JTable();

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("Laporan Penggajian");

        pnlFilter.setBorder(javax.swing.BorderFactory.createTitledBorder("Filter Tanggal"));

        lblTanggalAwal.setText("Dari:");

        spnTanggalAwal.setModel(new javax.swing.SpinnerDateModel());
        spnTanggalAwal.setEditor(new javax.swing.JSpinner.DateEditor(spnTanggalAwal, "dd-MM-yyyy"));

        lblTanggalAkhir.setText("Sampai:");

        spnTanggalAkhir.setModel(new javax.swing.SpinnerDateModel());
        spnTanggalAkhir.setEditor(new javax.swing.JSpinner.DateEditor(spnTanggalAkhir, "dd-MM-yyyy"));

        btnFilter.setText("Filter");
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });

        btnExport.setBackground(new java.awt.Color(0, 102, 153));
        btnExport.setForeground(new java.awt.Color(255, 255, 255));
        btnExport.setText("Export CSV");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlFilterLayout = new javax.swing.GroupLayout(pnlFilter);
        pnlFilter.setLayout(pnlFilterLayout);
        pnlFilterLayout.setHorizontalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTanggalAwal)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnTanggalAwal, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblTanggalAkhir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spnTanggalAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnFilter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnExport)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlFilterLayout.setVerticalGroup(
            pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFilterLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFilterLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTanggalAwal)
                    .addComponent(spnTanggalAwal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTanggalAkhir)
                    .addComponent(spnTanggalAkhir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnFilter)
                    .addComponent(btnExport))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblTotalData.setText("Total Data: 0 | Total Gaji: Rp 0");

        tblLaporan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {"ID", "Tanggal", "Nama Karyawan", "Jabatan", "Gaji Pokok", "Tunjangan", "Total Gaji"}
        ));
        jScrollPane1.setViewportView(tblLaporan);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTotalData)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        loadData();
    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        exportToCSV();
    }//GEN-LAST:event_btnExportActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnFilter;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTanggalAkhir;
    private javax.swing.JLabel lblTanggalAwal;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalData;
    private javax.swing.JPanel pnlFilter;
    private javax.swing.JSpinner spnTanggalAkhir;
    private javax.swing.JSpinner spnTanggalAwal;
    private javax.swing.JTable tblLaporan;
    // End of variables declaration//GEN-END:variables
}
