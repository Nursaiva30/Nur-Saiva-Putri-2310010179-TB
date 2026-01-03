import form.JabatanForm;
import form.KaryawanForm;
import form.PenggajianForm;
import laporan.LaporanKaryawan;
import laporan.LaporanPenggajian;

import javax.swing.*;
import java.awt.*;

/**
 * Main Frame Aplikasi Kepegawaian
 * Nur Saiva Putri - 2310010179
 */
public class MainFrame extends JFrame {
    
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Form instances
    private JabatanForm jabatanForm;
    private KaryawanForm karyawanForm;
    private PenggajianForm penggajianForm;
    private LaporanKaryawan laporanKaryawan;
    private LaporanPenggajian laporanPenggajian;
    
    public MainFrame() {
        setTitle("Aplikasi Kepegawaian - Nur Saiva Putri (2310010179)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        
        // Create menu bar
        setJMenuBar(createMenuBar());
        
        // Create content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Add welcome panel
        contentPanel.add(createWelcomePanel(), "welcome");
        
        // Initialize forms
        jabatanForm = new JabatanForm();
        karyawanForm = new KaryawanForm();
        penggajianForm = new PenggajianForm();
        laporanKaryawan = new LaporanKaryawan();
        laporanPenggajian = new LaporanPenggajian();
        
        // Add forms to content panel
        contentPanel.add(jabatanForm, "jabatan");
        contentPanel.add(karyawanForm, "karyawan");
        contentPanel.add(penggajianForm, "penggajian");
        contentPanel.add(laporanKaryawan, "laporan_karyawan");
        contentPanel.add(laporanPenggajian, "laporan_penggajian");
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Show welcome panel by default
        cardLayout.show(contentPanel, "welcome");
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // Menu Master Data
        JMenu menuMaster = new JMenu("Master Data");
        
        JMenuItem menuJabatan = new JMenuItem("Data Jabatan");
        menuJabatan.addActionListener(e -> cardLayout.show(contentPanel, "jabatan"));
        menuMaster.add(menuJabatan);
        
        JMenuItem menuKaryawan = new JMenuItem("Data Karyawan");
        menuKaryawan.addActionListener(e -> {
            karyawanForm.refreshJabatan();
            cardLayout.show(contentPanel, "karyawan");
        });
        menuMaster.add(menuKaryawan);
        
        menuBar.add(menuMaster);
        
        // Menu Transaksi
        JMenu menuTransaksi = new JMenu("Transaksi");
        
        JMenuItem menuPenggajian = new JMenuItem("Penggajian");
        menuPenggajian.addActionListener(e -> {
            penggajianForm.refreshKaryawan();
            cardLayout.show(contentPanel, "penggajian");
        });
        menuTransaksi.add(menuPenggajian);
        
        menuBar.add(menuTransaksi);
        
        // Menu Laporan
        JMenu menuLaporan = new JMenu("Laporan");
        
        JMenuItem menuLapKaryawan = new JMenuItem("Laporan Karyawan");
        menuLapKaryawan.addActionListener(e -> cardLayout.show(contentPanel, "laporan_karyawan"));
        menuLaporan.add(menuLapKaryawan);
        
        JMenuItem menuLapPenggajian = new JMenuItem("Laporan Penggajian");
        menuLapPenggajian.addActionListener(e -> cardLayout.show(contentPanel, "laporan_penggajian"));
        menuLaporan.add(menuLapPenggajian);
        
        menuBar.add(menuLaporan);
        
        // Menu Keluar
        JMenu menuApp = new JMenu("Aplikasi");
        
        JMenuItem menuHome = new JMenuItem("Beranda");
        menuHome.addActionListener(e -> cardLayout.show(contentPanel, "welcome"));
        menuApp.add(menuHome);
        
        menuApp.addSeparator();
        
        JMenuItem menuKeluar = new JMenuItem("Keluar");
        menuKeluar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Yakin ingin keluar dari aplikasi?", 
                "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        menuApp.add(menuKeluar);
        
        menuBar.add(menuApp);
        
        return menuBar;
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(30, 50, 30, 50)
        ));
        
        JLabel lblWelcome = new JLabel("Selamat Datang");
        lblWelcome.setFont(new Font("Dialog", Font.BOLD, 24));
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblApp = new JLabel("Aplikasi Sistem Kepegawaian");
        lblApp.setFont(new Font("Dialog", Font.PLAIN, 16));
        lblApp.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblLine = new JLabel("────────────────────");
        lblLine.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblInfo = new JLabel("<html><center>" +
            "Fitur Aplikasi:<br/><br/>" +
            "• CRUD Data Jabatan<br/>" +
            "• CRUD Data Karyawan<br/>" +
            "• Transaksi Penggajian<br/>" +
            "• Laporan Karyawan (CSV)<br/>" +
            "• Laporan Penggajian (CSV)" +
            "</center></html>");
        lblInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblAuthor = new JLabel("Nur Saiva Putri - 2310010179");
        lblAuthor.setFont(new Font("Dialog", Font.ITALIC, 11));
        lblAuthor.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(lblWelcome);
        card.add(Box.createVerticalStrut(5));
        card.add(lblApp);
        card.add(Box.createVerticalStrut(15));
        card.add(lblLine);
        card.add(Box.createVerticalStrut(15));
        card.add(lblInfo);
        card.add(Box.createVerticalStrut(20));
        card.add(lblAuthor);
        
        panel.add(card);
        
        return panel;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
