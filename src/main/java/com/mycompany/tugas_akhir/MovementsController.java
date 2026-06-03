package com.mycompany.tugas_akhir;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller untuk halaman Movements (Mutasi Barang).
 * Fitur: 4 Tab navigasi (All, Stock In, Stock Out, Adjustment)
 */
public class MovementsController implements Initializable {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final BarangDAO    barangDAO    = new BarangDAO();
    private final SupplierDAO  supplierDAO  = new SupplierDAO();

    private UserDAO.User currentUser;
    private List<BarangDAO.Barang> barangItems;

    // ── TAB BUTTONS ────────────────────────────────────────────────────────────
    @FXML private Button btnTabAll;
    @FXML private Button btnTabStockIn;
    @FXML private Button btnTabStockOut;
    @FXML private Button btnTabAdjustment;

    // ── TAB CONTENT PANES ──────────────────────────────────────────────────────
    @FXML private VBox tabContentAll;
    @FXML private HBox tabContentStockIn;
    @FXML private HBox tabContentStockOut;
    @FXML private HBox tabContentAdjustment;

    // ══ TAB ALL: DASHBOARD & ALL TRANSACTIONS ══════════════════════════════════
    @FXML private Label lblTotalMovements;
    @FXML private Label lblStockInCount;
    @FXML private Label lblStockOutCount;
    @FXML private Label lblAdjustmentCount;
    @FXML private Label lblAllTotalTx;
    @FXML private TableView<TransaksiDAO.Transaksi> tableAllTransaksi;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAllKodeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAllTipeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAllNamaBarang;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAllSupplierTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAllJumlahTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAllTanggal;

    private final ObservableList<TransaksiDAO.Transaksi> allTxList = FXCollections.observableArrayList();

    // ══ TAB STOCK IN ═══════════════════════════════════════════════════════════
    @FXML private ComboBox<BarangDAO.Barang> cbBarangStockIn;
    @FXML private ComboBox<SupplierDAO.Supplier> cbSupplierStockIn;
    @FXML private HBox boxInfoStokStockIn;
    @FXML private Label lblInfoStokStockIn;
    @FXML private TextField tfJumlahStockIn;
    @FXML private TextArea taKeteranganStockIn;
    @FXML private Label lblTotalStockIn;
    @FXML private TableView<TransaksiDAO.Transaksi> tableStockInTransaksi;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockInKodeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockInNamaBarang;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockInSupplier;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockInJumlah;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockInTanggal;

    private final ObservableList<TransaksiDAO.Transaksi> stockInTxList = FXCollections.observableArrayList();

    // ══ TAB STOCK OUT ══════════════════════════════════════════════════════════
    @FXML private ComboBox<BarangDAO.Barang> cbBarangStockOut;
    @FXML private HBox boxInfoStokStockOut;
    @FXML private Label lblInfoStokStockOut;
    @FXML private TextField tfTujuanStockOut;
    @FXML private TextField tfJumlahStockOut;
    @FXML private TextArea taKeteranganStockOut;
    @FXML private Label lblTotalStockOut;
    @FXML private TableView<TransaksiDAO.Transaksi> tableStockOutTransaksi;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockOutKodeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockOutNamaBarang;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockOutTujuan;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockOutJumlah;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colStockOutTanggal;

    private final ObservableList<TransaksiDAO.Transaksi> stockOutTxList = FXCollections.observableArrayList();

    // ══ TAB ADJUSTMENT ═════════════════════════════════════════════════════════
    @FXML private ComboBox<BarangDAO.Barang> cbBarangAdjustment;
    @FXML private HBox boxInfoStokAdjustment;
    @FXML private Label lblInfoStokAdjustment;
    @FXML private TextField tfStokBaruAdjustment;
    @FXML private ComboBox<String> cbAlasanAdjustment;
    @FXML private TextArea taKeteranganAdjustment;
    @FXML private Label lblTotalAdjustment;
    @FXML private TableView<TransaksiDAO.Transaksi> tableAdjustmentTransaksi;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAdjustmentKodeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAdjustmentNamaBarang;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAdjustmentAlasan;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAdjustmentKeterangan;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAdjustmentStok;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String> colAdjustmentTanggal;

    private final ObservableList<TransaksiDAO.Transaksi> adjustmentTxList = FXCollections.observableArrayList();

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupAllTab();
        setupStockInTab();
        setupStockOutTab();
        setupAdjustmentTab();
        switchTab("all"); // Default ke tab All
    }

    public void initUser(UserDAO.User user) {
        this.currentUser = user;
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TAB NAVIGATION
    // ════════════════════════════════════════════════════════════════════════════

    private void switchTab(String tab) {
        // Sembunyikan semua
        tabContentAll.setVisible(false);
        tabContentAll.setManaged(false);
        tabContentStockIn.setVisible(false);
        tabContentStockIn.setManaged(false);
        tabContentStockOut.setVisible(false);
        tabContentStockOut.setManaged(false);
        tabContentAdjustment.setVisible(false);
        tabContentAdjustment.setManaged(false);

        // Reset semua button style
        btnTabAll.setStyle("-fx-padding: 12 20 12 20; -fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");
        btnTabStockIn.setStyle("-fx-padding: 12 20 12 20; -fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");
        btnTabStockOut.setStyle("-fx-padding: 12 20 12 20; -fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");
        btnTabAdjustment.setStyle("-fx-padding: 12 20 12 20; -fx-font-size: 13px; -fx-text-fill: #6b7280; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-border-color: transparent; -fx-cursor: hand;");

        String activeStyle = "-fx-padding: 12 20 12 20; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1f2937; -fx-background-color: transparent; -fx-border-color: transparent; -fx-border-width: 0 0 2 0; -fx-border-color: #3b82f6; -fx-cursor: hand;";

        switch (tab) {
            case "all":
                tabContentAll.setVisible(true);
                tabContentAll.setManaged(true);
                btnTabAll.setStyle(activeStyle);
                loadAllTransaksi();
                break;
            case "stockIn":
                tabContentStockIn.setVisible(true);
                tabContentStockIn.setManaged(true);
                btnTabStockIn.setStyle(activeStyle);
                loadStockInTransaksi();
                break;
            case "stockOut":
                tabContentStockOut.setVisible(true);
                tabContentStockOut.setManaged(true);
                btnTabStockOut.setStyle(activeStyle);
                loadStockOutTransaksi();
                break;
            case "adjustment":
                tabContentAdjustment.setVisible(true);
                tabContentAdjustment.setManaged(true);
                btnTabAdjustment.setStyle(activeStyle);
                loadAdjustmentTransaksi();
                break;
        }
    }

    @FXML private void handleTabAll() { switchTab("all"); }
    @FXML private void handleTabStockIn() { switchTab("stockIn"); }
    @FXML private void handleTabStockOut() { switchTab("stockOut"); }
    @FXML private void handleTabAdjustment() { switchTab("adjustment"); }

    // ════════════════════════════════════════════════════════════════════════════
    // TAB ALL - SETUP & LOAD
    // ════════════════════════════════════════════════════════════════════════════

    private void setupAllTab() {
        colAllKodeTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kodeTransaksi));
        colAllTipeTx.setCellValueFactory(d -> new SimpleStringProperty(
            "masuk".equals(d.getValue().tipe) ? "📥 Masuk" : "📤 Keluar"));
        colAllTipeTx.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(s.contains("Masuk") ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });
        colAllNamaBarang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaBarang));
        colAllSupplierTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaSupplier));
        colAllJumlahTx.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().jumlah)));
        colAllTanggal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tanggal));
        tableAllTransaksi.setItems(allTxList);
    }

    private void loadAllTransaksi() {
        allTxList.setAll(transaksiDAO.getAllTransaksi());
        lblAllTotalTx.setText(allTxList.size() + " transaksi");
        updateStatistics();
    }

    private void updateStatistics() {
        int totalMovements = allTxList.size();
        int stockInCount = (int) allTxList.stream()
            .filter(t -> "masuk".equals(t.tipe) && 
                        !isAdjustmentReason(t.keterangan))
            .count();
        int stockOutCount = (int) allTxList.stream()
            .filter(t -> "keluar".equals(t.tipe) && 
                        !isAdjustmentReason(t.keterangan))
            .count();
        int adjustmentCount = (int) allTxList.stream()
            .filter(t -> isAdjustmentReason(t.keterangan))
            .count();

        lblTotalMovements.setText(String.valueOf(totalMovements));
        lblStockInCount.setText(String.valueOf(stockInCount));
        lblStockOutCount.setText(String.valueOf(stockOutCount));
        lblAdjustmentCount.setText(String.valueOf(adjustmentCount));
    }

    private boolean isAdjustmentReason(String keterangan) {
        if (keterangan == null) return false;
        return keterangan.contains("Stok Opname") || 
               keterangan.contains("Kerusakan") || 
               keterangan.contains("Kadaluarsa") || 
               keterangan.contains("Penyesuaian Sistem") ||
               keterangan.contains("Lainnya");
    }

    @FXML private void handleRefreshAll() { loadAllTransaksi(); }

    // ════════════════════════════════════════════════════════════════════════════
    // TAB STOCK IN - SETUP & LOAD
    // ════════════════════════════════════════════════════════════════════════════

    private void setupStockInTab() {
        barangItems = barangDAO.getAllBarang();
        cbBarangStockIn.setItems(FXCollections.observableArrayList(barangItems));
        cbSupplierStockIn.setItems(FXCollections.observableArrayList(supplierDAO.getAllSupplier()));

        colStockInKodeTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kodeTransaksi));
        colStockInNamaBarang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaBarang));
        colStockInSupplier.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaSupplier));
        colStockInJumlah.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().jumlah)));
        colStockInTanggal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tanggal));
        tableStockInTransaksi.setItems(stockInTxList);
    }

    @FXML private void handleBarangChangeStockIn() {
        BarangDAO.Barang selected = cbBarangStockIn.getValue();
        if (selected != null) {
            lblInfoStokStockIn.setText("Stok saat ini: " + selected.stok + " " + selected.satuan);
            boxInfoStokStockIn.setVisible(true);
            boxInfoStokStockIn.setManaged(true);
        } else {
            boxInfoStokStockIn.setVisible(false);
            boxInfoStokStockIn.setManaged(false);
        }
    }

    @FXML private void handleSimpanStockIn() {
        BarangDAO.Barang barang = cbBarangStockIn.getValue();
        String jumlahStr = tfJumlahStockIn.getText().trim();

        if (barang == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih barang terlebih dahulu!");
            return;
        }
        if (jumlahStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah tidak boleh kosong!");
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah harus berupa angka positif!");
            return;
        }

        SupplierDAO.Supplier supplier = cbSupplierStockIn.getValue();
        Integer supplierId = (supplier != null) ? supplier.id : null;
        String keterangan = taKeteranganStockIn.getText().trim();
        int userId = (currentUser != null) ? currentUser.id : 1;

        boolean ok = transaksiDAO.addTransaksi("masuk", barang.id, supplierId, null, jumlah, keterangan, userId);

        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "✅ Stock In berhasil dicatat!");
            clearStockInForm();
            loadStockInTransaksi();
            loadAllTransaksi();
            barangItems = barangDAO.getAllBarang();
            cbBarangStockIn.setItems(FXCollections.observableArrayList(barangItems));
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Stock In gagal disimpan.");
        }
    }

    private void loadStockInTransaksi() {
        stockInTxList.setAll(transaksiDAO.getAllTransaksi().stream()
            .filter(t -> "masuk".equals(t.tipe))
            .toList());
        lblTotalStockIn.setText(stockInTxList.size() + " transaksi");
    }

    @FXML private void handleRefreshStockIn() { loadStockInTransaksi(); }

    private void clearStockInForm() {
        cbBarangStockIn.setValue(null);
        cbSupplierStockIn.setValue(null);
        tfJumlahStockIn.clear();
        taKeteranganStockIn.clear();
        boxInfoStokStockIn.setVisible(false);
        boxInfoStokStockIn.setManaged(false);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TAB STOCK OUT - SETUP & LOAD
    // ════════════════════════════════════════════════════════════════════════════

    private void setupStockOutTab() {
        cbBarangStockOut.setItems(FXCollections.observableArrayList(barangDAO.getAllBarang()));

        colStockOutKodeTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kodeTransaksi));
        colStockOutNamaBarang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaBarang));
        colStockOutTujuan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaSupplier));
        colStockOutJumlah.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().jumlah)));
        colStockOutTanggal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tanggal));
        tableStockOutTransaksi.setItems(stockOutTxList);
    }

    @FXML private void handleBarangChangeStockOut() {
        BarangDAO.Barang selected = cbBarangStockOut.getValue();
        if (selected != null) {
            lblInfoStokStockOut.setText("Stok saat ini: " + selected.stok + " " + selected.satuan);
            boxInfoStokStockOut.setVisible(true);
            boxInfoStokStockOut.setManaged(true);
        } else {
            boxInfoStokStockOut.setVisible(false);
            boxInfoStokStockOut.setManaged(false);
        }
    }

    @FXML private void handleSimpanStockOut() {
        BarangDAO.Barang barang = cbBarangStockOut.getValue();
        String jumlahStr = tfJumlahStockOut.getText().trim();

        if (barang == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih barang terlebih dahulu!");
            return;
        }
        if (jumlahStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah tidak boleh kosong!");
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Jumlah harus berupa angka positif!");
            return;
        }

        if (barang.stok < jumlah) {
            showAlert(Alert.AlertType.WARNING, "Stok Tidak Cukup",
                "Stok " + barang.namaBarang + " saat ini hanya " + barang.stok);
            return;
        }

        String keterangan = taKeteranganStockOut.getText().trim();
        int userId = (currentUser != null) ? currentUser.id : 1;

        boolean ok = transaksiDAO.addTransaksi("keluar", barang.id, null, null, jumlah, keterangan, userId);

        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "✅ Stock Out berhasil dicatat!");
            clearStockOutForm();
            loadStockOutTransaksi();
            loadAllTransaksi();
            barangItems = barangDAO.getAllBarang();
            cbBarangStockOut.setItems(FXCollections.observableArrayList(barangItems));
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Stock Out gagal disimpan.");
        }
    }

    private void loadStockOutTransaksi() {
        stockOutTxList.setAll(transaksiDAO.getAllTransaksi().stream()
            .filter(t -> "keluar".equals(t.tipe))
            .toList());
        lblTotalStockOut.setText(stockOutTxList.size() + " transaksi");
    }

    @FXML private void handleRefreshStockOut() { loadStockOutTransaksi(); }

    private void clearStockOutForm() {
        cbBarangStockOut.setValue(null);
        tfTujuanStockOut.clear();
        tfJumlahStockOut.clear();
        taKeteranganStockOut.clear();
        boxInfoStokStockOut.setVisible(false);
        boxInfoStokStockOut.setManaged(false);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TAB ADJUSTMENT - SETUP & LOAD
    // ════════════════════════════════════════════════════════════════════════════

    private void setupAdjustmentTab() {
        cbBarangAdjustment.setItems(FXCollections.observableArrayList(barangDAO.getAllBarang()));
        cbAlasanAdjustment.setItems(FXCollections.observableArrayList(
            "Stok Opname", "Kerusakan", "Kadaluarsa", "Penyesuaian Sistem", "Lainnya"));

        colAdjustmentKodeTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kodeTransaksi));
        colAdjustmentNamaBarang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaBarang));
        colAdjustmentAlasan.setCellValueFactory(d -> {
            String ket = d.getValue().keterangan;
            if (ket != null && ket.contains(" - ")) {
                return new SimpleStringProperty(ket.substring(0, ket.indexOf(" - ")));
            }
            return new SimpleStringProperty(ket != null ? ket : "-");
        });
        colAdjustmentKeterangan.setCellValueFactory(d -> {
            String ket = d.getValue().keterangan;
            if (ket != null && ket.contains(" - ")) {
                return new SimpleStringProperty(ket.substring(ket.indexOf(" - ") + 3));
            }
            return new SimpleStringProperty("-");
        });
        colAdjustmentStok.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().jumlah)));
        colAdjustmentTanggal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tanggal));
        tableAdjustmentTransaksi.setItems(adjustmentTxList);
    }

    @FXML private void handleBarangChangeAdjustment() {
        BarangDAO.Barang selected = cbBarangAdjustment.getValue();
        if (selected != null) {
            lblInfoStokAdjustment.setText("Stok saat ini: " + selected.stok + " " + selected.satuan);
            boxInfoStokAdjustment.setVisible(true);
            boxInfoStokAdjustment.setManaged(true);
        } else {
            boxInfoStokAdjustment.setVisible(false);
            boxInfoStokAdjustment.setManaged(false);
        }
    }

    @FXML private void handleSimpanAdjustment() {
        BarangDAO.Barang barang = cbBarangAdjustment.getValue();
        String stokBaruStr = tfStokBaruAdjustment.getText().trim();
        String alasan = cbAlasanAdjustment.getValue();

        if (barang == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih barang terlebih dahulu!");
            return;
        }
        if (stokBaruStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok baru tidak boleh kosong!");
            return;
        }
        if (alasan == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih alasan penyesuaian!");
            return;
        }

        int stokBaru;
        try {
            stokBaru = Integer.parseInt(stokBaruStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Stok harus berupa angka!");
            return;
        }

        String keterangan = taKeteranganAdjustment.getText().trim();
        int userId = (currentUser != null) ? currentUser.id : 1;

        int selisih = stokBaru - barang.stok;
        String tipe = selisih > 0 ? "masuk" : "keluar";
        int jumlah = Math.abs(selisih);

        boolean ok = transaksiDAO.addTransaksi(tipe, barang.id, null, null, jumlah, 
            alasan + " - " + keterangan, userId);

        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "✅ Adjustment berhasil dicatat!");
            clearAdjustmentForm();
            loadAdjustmentTransaksi();
            loadAllTransaksi();
            barangItems = barangDAO.getAllBarang();
            cbBarangAdjustment.setItems(FXCollections.observableArrayList(barangItems));
        } else {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Adjustment gagal disimpan.");
        }
    }

    private void loadAdjustmentTransaksi() {
        adjustmentTxList.setAll(transaksiDAO.getAllTransaksi().stream()
            .filter(t -> "Stok Opname".equals(t.keterangan) || 
                        "Kerusakan".equals(t.keterangan) || 
                        "Kadaluarsa".equals(t.keterangan) || 
                        "Penyesuaian Sistem".equals(t.keterangan) || 
                        "Lainnya".equals(t.keterangan) ||
                        t.keterangan.contains("Stok Opname") ||
                        t.keterangan.contains("Kerusakan") ||
                        t.keterangan.contains("Kadaluarsa") ||
                        t.keterangan.contains("Penyesuaian Sistem"))
            .toList());
        lblTotalAdjustment.setText(adjustmentTxList.size() + " transaksi");
    }

    @FXML private void handleRefreshAdjustment() { loadAdjustmentTransaksi(); }

    private void clearAdjustmentForm() {
        cbBarangAdjustment.setValue(null);
        cbAlasanAdjustment.setValue(null);
        tfStokBaruAdjustment.clear();
        taKeteranganAdjustment.clear();
        boxInfoStokAdjustment.setVisible(false);
        boxInfoStokAdjustment.setManaged(false);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EXPORT PDF METHODS
    // ════════════════════════════════════════════════════════════════════════════

    @FXML private void handleExportAll() { exportMovementsToPDF(allTxList, "Laporan Semua Movement"); }
    @FXML private void handleExportStockIn() { exportMovementsToPDF(stockInTxList, "Laporan Stock In"); }
    @FXML private void handleExportStockOut() { exportMovementsToPDF(stockOutTxList, "Laporan Stock Out"); }
    @FXML private void handleExportAdjustment() { exportMovementsToPDF(adjustmentTxList, "Laporan Adjustment"); }

    private void exportMovementsToPDF(ObservableList<TransaksiDAO.Transaksi> txList, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan File PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(title.replace(" ", "_") + ".pdf");
        
        File selectedFile = fileChooser.showSaveDialog(null);
        if (selectedFile != null) {
            try {
                exportToPDF(selectedFile, txList, title);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", 
                    "File PDF berhasil disimpan di:\n" + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error Export", 
                    "Gagal mengekspor ke PDF:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToPDF(File file, ObservableList<TransaksiDAO.Transaksi> txList, String title) throws Exception {
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(file);
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
        
        // Header
        com.itextpdf.layout.element.Paragraph titlePara = new com.itextpdf.layout.element.Paragraph(title)
            .setFontSize(18)
            .setBold()
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
        document.add(titlePara);
        
        com.itextpdf.layout.element.Paragraph date = new com.itextpdf.layout.element.Paragraph(
            "Tanggal: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")))
            .setFontSize(10)
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
            .setMarginBottom(12);
        document.add(date);
        
        // Stats
        long masuk = txList.stream().filter(t -> "masuk".equals(t.tipe)).count();
        long keluar = txList.stream().filter(t -> "keluar".equals(t.tipe)).count();
        
        com.itextpdf.layout.element.Paragraph stats = new com.itextpdf.layout.element.Paragraph(
            "Total Transaksi: " + txList.size() + " | Masuk: " + masuk + " | Keluar: " + keluar)
            .setFontSize(10)
            .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
            .setMarginBottom(16);
        document.add(stats);
        
        // Table
        com.itextpdf.layout.element.Table table = new com.itextpdf.layout.element.Table(
            new float[]{30, 120, 100, 100, 80, 100});
        table.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
        
        // Header cells
        String[] headers = {"#", "KODE", "BARANG", "TIPE", "JUMLAH", "TANGGAL"};
        for (String h : headers) {
            table.addHeaderCell(h).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
        }
        
        // Data rows
        int no = 1;
        for (TransaksiDAO.Transaksi t : txList) {
            String tipe = "masuk".equals(t.tipe) ? "Masuk" : "Keluar";
            
            table.addCell(String.valueOf(no++));
            table.addCell(t.kodeTransaksi);
            table.addCell(t.namaBarang);
            table.addCell(tipe);
            table.addCell(String.valueOf(t.jumlah));
            table.addCell(t.tanggal);
        }
        
        document.add(table);
        document.close();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ════════════════════════════════════════════════════════════════════════════

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
