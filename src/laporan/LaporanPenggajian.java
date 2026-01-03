package laporan;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Laporan Penggajian dengan Filter Periode dan Export ke CSV
 */
public class LaporanPenggajian extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtDateFrom, txtDateTo;
    private JLabel lblTotalGaji;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    public LaporanPenggajian() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Laporan Penggajian", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        
        // Filter Panel
        JPanel filterPanel = createFilterPanel();
        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Summary Panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createEtchedBorder());
        
        lblTotalGaji = new JLabel("Total Gaji: Rp 0 | Jumlah Transaksi: 0");
        lblTotalGaji.setFont(new Font("Dialog", Font.BOLD, 12));
        summaryPanel.add(lblTotalGaji);
        
        add(summaryPanel, BorderLayout.SOUTH);
        
        loadData();
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Filter Periode"));
        
        panel.add(new JLabel("Dari:"));
        txtDateFrom = new JTextField(10);
        txtDateFrom.setToolTipText("Format: YYYY-MM-DD");
        panel.add(txtDateFrom);
        
        panel.add(new JLabel("Sampai:"));
        txtDateTo = new JTextField(10);
        txtDateTo.setToolTipText("Format: YYYY-MM-DD");
        panel.add(txtDateTo);
        
        JButton btnFilter = new JButton("Filter");
        btnFilter.addActionListener(e -> loadData());
        panel.add(btnFilter);
        
        JButton btnReset = new JButton("Reset");
        btnReset.addActionListener(e -> {
            txtDateFrom.setText("");
            txtDateTo.setText("");
            loadData();
        });
        panel.add(btnReset);
        
        JButton btnExport = new JButton("Export CSV");
        btnExport.addActionListener(e -> exportToCSV());
        panel.add(btnExport);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Riwayat Penggajian"));
        
        String[] columns = {"No", "ID", "Nama Karyawan", "Jabatan", "Tanggal", "Gaji Pokok", "Tunjangan", "Potongan", "Total Gaji", "Keterangan"};
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
        table.getColumnModel().getColumn(1).setPreferredWidth(30);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(80);
        table.getColumnModel().getColumn(7).setPreferredWidth(70);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);
        table.getColumnModel().getColumn(9).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        double totalGajiSum = 0;
        
        StringBuilder query = new StringBuilder("""
            SELECT p.id_penggajian, k.nama_karyawan, j.nama_jabatan,
                   p.tanggal_gaji, j.gaji_pokok, j.tunjangan, 
                   p.potongan, p.total_gaji, p.keterangan
            FROM penggajian p
            JOIN karyawan k ON p.id_karyawan = k.id_karyawan
            LEFT JOIN jabatan j ON k.id_jabatan = j.id_jabatan
            WHERE 1=1
            """);
        
        String dateFrom = txtDateFrom.getText().trim();
        String dateTo = txtDateTo.getText().trim();
        
        if (!dateFrom.isEmpty()) {
            query.append(" AND p.tanggal_gaji >= '").append(dateFrom).append("'");
        }
        if (!dateTo.isEmpty()) {
            query.append(" AND p.tanggal_gaji <= '").append(dateTo).append("'");
        }
        query.append(" ORDER BY p.tanggal_gaji DESC, p.id_penggajian DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString())) {
            
            int no = 1;
            while (rs.next()) {
                double totalGaji = rs.getDouble("total_gaji");
                totalGajiSum += totalGaji;
                
                Object[] row = {
                    no++,
                    rs.getInt("id_penggajian"),
                    rs.getString("nama_karyawan"),
                    rs.getString("nama_jabatan"),
                    rs.getDate("tanggal_gaji").toString(),
                    currencyFormat.format(rs.getDouble("gaji_pokok")),
                    currencyFormat.format(rs.getDouble("tunjangan")),
                    currencyFormat.format(rs.getDouble("potongan")),
                    currencyFormat.format(totalGaji),
                    rs.getString("keterangan")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        lblTotalGaji.setText("Total Gaji: " + currencyFormat.format(totalGajiSum) + 
            " | Jumlah Transaksi: " + tableModel.getRowCount());
    }
    
    private void exportToCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk di-export!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Penggajian");
        fileChooser.setSelectedFile(new java.io.File("laporan_penggajian.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.write("No,ID,Nama Karyawan,Jabatan,Tanggal Gaji,Gaji Pokok,Tunjangan,Potongan,Total Gaji,Keterangan\n");
                
                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    StringBuilder row = new StringBuilder();
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object value = tableModel.getValueAt(i, j);
                        String cellValue = value != null ? value.toString().replace(",", ";") : "";
                        row.append(cellValue);
                        if (j < tableModel.getColumnCount() - 1) {
                            row.append(",");
                        }
                    }
                    row.append("\n");
                    writer.write(row.toString());
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Laporan berhasil di-export ke:\n" + filePath, 
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error export: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
