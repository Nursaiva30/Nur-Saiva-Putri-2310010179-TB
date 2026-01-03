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
 * Laporan Data Karyawan dengan Export ke CSV
 */
public class LaporanKaryawan extends JPanel {
    
    private JTable table;
    private DefaultTableModel tableModel;
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    
    public LaporanKaryawan() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel lblTitle = new JLabel("Laporan Data Karyawan", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 18));
        headerPanel.add(lblTitle, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export CSV");
        
        btnRefresh.addActionListener(e -> loadData());
        btnExport.addActionListener(e -> exportToCSV());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnExport);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        loadData();
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Daftar Karyawan"));
        
        String[] columns = {"No", "ID", "Nama Karyawan", "Jenis Kelamin", "Jabatan", "Gaji Pokok", "Tunjangan", "No. Telepon", "Alamat"};
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
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(180);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadData() {
        tableModel.setRowCount(0);
        String query = """
            SELECT k.id_karyawan, k.nama_karyawan, k.jenis_kelamin, 
                   j.nama_jabatan, j.gaji_pokok, j.tunjangan,
                   k.no_telepon, k.alamat
            FROM karyawan k
            LEFT JOIN jabatan j ON k.id_jabatan = j.id_jabatan
            ORDER BY k.id_karyawan
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            int no = 1;
            while (rs.next()) {
                String jk = rs.getString("jenis_kelamin");
                String jenisKelamin = jk.equals("L") ? "Laki-laki" : "Perempuan";
                
                Object[] row = {
                    no++,
                    rs.getInt("id_karyawan"),
                    rs.getString("nama_karyawan"),
                    jenisKelamin,
                    rs.getString("nama_jabatan"),
                    currencyFormat.format(rs.getDouble("gaji_pokok")),
                    currencyFormat.format(rs.getDouble("tunjangan")),
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
    
    private void exportToCSV() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk di-export!", 
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Laporan Karyawan");
        fileChooser.setSelectedFile(new java.io.File("laporan_karyawan.csv"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.write("No,ID,Nama Karyawan,Jenis Kelamin,Jabatan,Gaji Pokok,Tunjangan,No Telepon,Alamat\n");
                
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
