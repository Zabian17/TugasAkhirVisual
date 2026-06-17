package com.mycompany.tugas_akhir;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * Controller untuk halaman Report.
 * Menampilkan laporan transaksi berdasarkan filter periode dan tipe.
 */
public class ReportController implements Initializable {

    // ── DAO ───────────────────────────────────────────────────────────────────
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    // ── Format tanggal untuk query DB ─────────────────────────────────────────
    private static final DateTimeFormatter DB_FORMAT   = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── FXML Filter ───────────────────────────────────────────────────────────
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private ComboBox<String> cbTipe;
    @FXML private HBox boxPeriodeInfo;
    @FXML private Label lblPeriodeInfo;

    // ── FXML Summary Cards ────────────────────────────────────────────────────
    @FXML private Label lblTotalTx;
    @FXML private Label lblTotalMasuk;
    @FXML private Label lblTotalKeluar;
    @FXML private Label lblBarangAktif;

    // ── FXML Tabel ────────────────────────────────────────────────────────────
    @FXML private TableView<ReportRow> tableReport;
    @FXML private TableColumn<ReportRow, Integer> colNo;
    @FXML private TableColumn<ReportRow, String>  colKodeTx;
    @FXML private TableColumn<ReportRow, String>  colTipe;
    @FXML private TableColumn<ReportRow, String>  colBarang;
    @FXML private TableColumn<ReportRow, String>  colSupplier;
    @FXML private TableColumn<ReportRow, String>  colJumlah;
    @FXML private TableColumn<ReportRow, String>  colRak;
    @FXML private TableColumn<ReportRow, String>  colTanggal;
    @FXML private Label lblJumlahData;

    // ── FXML Top Barang ───────────────────────────────────────────────────────
    @FXML private VBox vboxTopBarang;

    // ── Model row untuk TableView ─────────────────────────────────────────────
    public static class ReportRow {
        private final int    no;
        private final String kodeTx;
        private final String tipe;
        private final String barang;
        private final String supplier;
        private final String jumlah;
        private final String rak;
        private final String tanggal;

        public ReportRow(int no, String kodeTx, String tipe, String barang,
                         String supplier, String jumlah, String rak, String tanggal) {
            this.no       = no;
            this.kodeTx   = kodeTx;
            this.tipe     = tipe;
            this.barang   = barang;
            this.supplier = supplier;
            this.jumlah   = jumlah;
            this.rak      = rak;
            this.tanggal  = tanggal;
        }

        public int    getNo()       { return no; }
        public String getKodeTx()   { return kodeTx; }
        public String getTipe()     { return tipe.substring(0, 1).toUpperCase() + tipe.substring(1); }
        public String getBarang()   { return barang; }
        public String getSupplier() { return supplier; }
        public String getJumlah()   { return jumlah; }
        public String getRak()      { return rak; }
        public String getTanggal()  { return tanggal; }
    }

    // ── Initialize ────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboTipe();
        setupTableColumns();
        setDefaultDateRange();
        // Auto-load data dengan periode default
        handleGenerateReport();
    }

    /**
     * Dipanggil oleh DashboardController setelah load FXML.
     */
    public void initUser(UserDAO.User user) {
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupComboTipe() {
        cbTipe.setItems(FXCollections.observableArrayList("Semua", "Masuk", "Keluar"));
        cbTipe.setValue("Semua");
    }

    private void setupTableColumns() {
        colNo.setCellValueFactory(new PropertyValueFactory<>("no"));
        colKodeTx.setCellValueFactory(new PropertyValueFactory<>("kodeTx"));
        colBarang.setCellValueFactory(new PropertyValueFactory<>("barang"));
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colRak.setCellValueFactory(new PropertyValueFactory<>("rak"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));

        // Custom cell factory untuk kolom Tipe (warna berbeda masuk/keluar)
        colTipe.setCellValueFactory(new PropertyValueFactory<>("tipe"));
        colTipe.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Masuk")) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 12px;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 12px;");
                    }
                }
            }
        });

        // Kolom Jumlah: kanan align
        colJumlah.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #374151;");
                }
            }
        });
    }

    private void setDefaultDateRange() {
        LocalDate today = LocalDate.now();
        dpEndDate.setValue(today);
        dpStartDate.setValue(today.minusDays(29)); // 30 hari terakhir
    }

    // ── FXML Handlers ─────────────────────────────────────────────────────────

    @FXML
    private void handleGenerateReport() {
        LocalDate start = dpStartDate.getValue();
        LocalDate end   = dpEndDate.getValue();

        if (start == null || end == null) {
            showAlert("Validasi", "Silakan pilih tanggal awal dan akhir.");
            return;
        }
        if (start.isAfter(end)) {
            showAlert("Validasi", "Tanggal awal tidak boleh lebih besar dari tanggal akhir.");
            return;
        }

        String startStr = start.format(DB_FORMAT);
        String endStr   = end.format(DB_FORMAT);
        String tipe     = cbTipe.getValue() != null ? cbTipe.getValue() : "Semua";

        loadSummary(startStr, endStr);
        loadTable(startStr, endStr, tipe);
        loadTopBarang(startStr, endStr);

        // Tampilkan info periode
        lblPeriodeInfo.setText("Menampilkan data periode: "
            + start.format(DISP_FORMAT) + "  →  " + end.format(DISP_FORMAT)
            + "   |   Tipe: " + tipe);
        boxPeriodeInfo.setVisible(true);
        boxPeriodeInfo.setManaged(true);
    }

    @FXML
    private void handleReset() {
        setDefaultDateRange();
        cbTipe.setValue("Semua");
        tableReport.getItems().clear();
        lblTotalTx.setText("0");
        lblTotalMasuk.setText("0");
        lblTotalKeluar.setText("0");
        lblBarangAktif.setText("0");
        lblJumlahData.setText("0 data");
        vboxTopBarang.getChildren().clear();
        vboxTopBarang.getChildren().add(
            buildInfoLabel("Generate report untuk melihat top barang")
        );
        boxPeriodeInfo.setVisible(false);
        boxPeriodeInfo.setManaged(false);
        // Auto-reload dengan range default
        handleGenerateReport();
    }

    @FXML
    private void handleRefresh() {
        handleGenerateReport();
    }

    @FXML
    private void handleExportCSV() {
        // Export sederhana: tampilkan dialog konfirmasi data
        ObservableList<ReportRow> data = tableReport.getItems();
        if (data.isEmpty()) {
            showAlert("Export CSV", "Tidak ada data untuk di-export. Silakan generate report terlebih dahulu.");
            return;
        }

        // Build CSV string
        StringBuilder csv = new StringBuilder();
        csv.append("No,Kode Transaksi,Tipe,Barang,Supplier/Info,Jumlah,Rak,Tanggal\n");
        for (ReportRow row : data) {
            csv.append(row.getNo()).append(",");
            csv.append(escapeCsv(row.getKodeTx())).append(",");
            csv.append(escapeCsv(row.getTipe())).append(",");
            csv.append(escapeCsv(row.getBarang())).append(",");
            csv.append(escapeCsv(row.getSupplier())).append(",");
            csv.append(escapeCsv(row.getJumlah())).append(",");
            csv.append(escapeCsv(row.getRak())).append(",");
            csv.append(escapeCsv(row.getTanggal())).append("\n");
        }

        // Simpan ke file di folder user home
        String fileName = "laporan_gudang_" + LocalDate.now().format(DB_FORMAT) + ".csv";
        java.io.File file = new java.io.File(System.getProperty("user.home"), fileName);
        try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
            fw.write(csv.toString());
            showInfo("Export Berhasil",
                "File CSV berhasil disimpan di:\n" + file.getAbsolutePath() +
                "\n\nTotal data: " + data.size() + " baris.");
        } catch (java.io.IOException e) {
            showAlert("Export Gagal", "Terjadi kesalahan saat menyimpan file: " + e.getMessage());
        }
    }

    // ── Data Loading ──────────────────────────────────────────────────────────

    private void loadSummary(String startDate, String endDate) {
        int[] summary = transaksiDAO.getReportSummary(startDate, endDate);
        lblTotalTx.setText(formatAngka(summary[0]));
        lblTotalMasuk.setText(formatAngka(summary[1]));
        lblTotalKeluar.setText(formatAngka(summary[2]));
        lblBarangAktif.setText(formatAngka(summary[3]));
    }

    private void loadTable(String startDate, String endDate, String tipe) {
        List<TransaksiDAO.Transaksi> list = transaksiDAO.getTransaksiByPeriod(startDate, endDate, tipe);

        ObservableList<ReportRow> rows = FXCollections.observableArrayList();
        int no = 1;
        for (TransaksiDAO.Transaksi t : list) {
            rows.add(new ReportRow(
                no++,
                t.kodeTransaksi,
                t.tipe,
                t.namaBarang,
                t.namaSupplier,
                formatAngka(t.jumlah) + " pcs",
                t.kodeRak,
                t.tanggal
            ));
        }

        tableReport.setItems(rows);
        lblJumlahData.setText(rows.size() + " data");
        System.out.println("[ReportController] Loaded " + rows.size() + " transaksi.");
    }

    private void loadTopBarang(String startDate, String endDate) {
        List<TransaksiDAO.TopBarang> topList = transaksiDAO.getTopBarang(startDate, endDate, 5);
        vboxTopBarang.getChildren().clear();

        if (topList.isEmpty()) {
            vboxTopBarang.getChildren().add(
                buildInfoLabel("Tidak ada data di periode ini")
            );
            return;
        }

        String[] rankColors = {"#f59e0b", "#9ba3b8", "#b45309", "#1a56db", "#6b7280"};
        String[] medals     = {"🥇", "🥈", "🥉", "4", "5"};

        int rank = 0;
        for (TransaksiDAO.TopBarang tb : topList) {
            String color  = rankColors[Math.min(rank, rankColors.length - 1)];
            String medal  = medals[Math.min(rank, medals.length - 1)];

            // Kontainer satu baris ranking
            HBox row = new HBox(8);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Badge ranking
            Label lblMedal = new Label(medal);
            lblMedal.setStyle("-fx-font-size: 16px;");

            // Info barang
            VBox info = new VBox(2);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label lblNama = new Label(tb.namaBarang);
            lblNama.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-wrap-text: true;");
            lblNama.setMaxWidth(Double.MAX_VALUE);

            Label lblDetail = new Label(tb.kodeBarang + " · " + formatAngka(tb.totalJumlah) + " pcs");
            lblDetail.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ba3b8;");

            info.getChildren().addAll(lblNama, lblDetail);

            // Total
            Label lblTotal = new Label(String.valueOf(tb.totalTransaksi) + " tx");
            lblTotal.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color
                + "; -fx-background-color: derive(" + color + ", 85%); -fx-background-radius: 6;"
                + " -fx-padding: 2 6 2 6;");

            row.getChildren().addAll(lblMedal, info, lblTotal);

            vboxTopBarang.getChildren().add(row);

            // Separator (kecuali baris terakhir)
            if (rank < topList.size() - 1) {
                Pane sep = new Pane();
                sep.setStyle("-fx-background-color: #f0f2f5; -fx-pref-height: 1px; -fx-min-height: 1px; -fx-max-height: 1px;");
                vboxTopBarang.getChildren().add(sep);
            }

            rank++;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Label buildInfoLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #9ba3b8; -fx-font-size: 11px; -fx-wrap-text: true;");
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    private String formatAngka(int n) {
        return String.format("%,d", n).replace(',', '.');
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
