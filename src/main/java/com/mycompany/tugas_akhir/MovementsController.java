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
import javafx.scene.layout.VBox;

/**
 * Controller untuk halaman Movements (Mutasi Barang).
 * Fitur: Input transaksi masuk/keluar, riwayat transaksi.
 */
public class MovementsController implements Initializable {

    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final BarangDAO    barangDAO    = new BarangDAO();
    private final SupplierDAO  supplierDAO  = new SupplierDAO();

    // Data user yang sedang login (di-pass dari DashboardController)
    private UserDAO.User currentUser;

    private final ObservableList<TransaksiDAO.Transaksi> txList = FXCollections.observableArrayList();
    private List<BarangDAO.Barang> barangItems;

    // ── FXML Refs: Form ───────────────────────────────────────────────────────
    @FXML private ComboBox<String>              cbTipe;
    @FXML private ComboBox<BarangDAO.Barang>    cbBarang;
    @FXML private ComboBox<SupplierDAO.Supplier> cbSupplier;
    @FXML private VBox                          boxSupplier;
    @FXML private VBox                          boxInfoStok;
    @FXML private Label                         lblInfoStok;
    @FXML private TextField                     tfJumlah;
    @FXML private TextArea                      taKeterangan;

    // ── FXML Refs: Table ──────────────────────────────────────────────────────
    @FXML private Label                                          lblTotalTx;
    @FXML private TableView<TransaksiDAO.Transaksi>             tableTransaksi;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String>   colKodeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String>   colTipeTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String>   colNamaBarang;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String>   colSupplierTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String>   colJumlahTx;
    @FXML private TableColumn<TransaksiDAO.Transaksi, String>   colTanggal;

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupForm();
        setupTable();
        loadTransaksi();
    }

    /**
     * Dipanggil oleh DashboardController untuk meneruskan data user login.
     */
    public void initUser(UserDAO.User user) {
        this.currentUser = user;
    }

    // ── Setup Form ────────────────────────────────────────────────────────────
    private void setupForm() {
        // Isi pilihan tipe
        cbTipe.setItems(FXCollections.observableArrayList("Barang Masuk", "Barang Keluar"));

        // Isi dropdown barang
        barangItems = barangDAO.getAllBarang();
        cbBarang.setItems(FXCollections.observableArrayList(barangItems));

        // Isi dropdown supplier
        cbSupplier.setItems(FXCollections.observableArrayList(supplierDAO.getAllSupplier()));
    }

    // ── Setup Table ───────────────────────────────────────────────────────────
    private void setupTable() {
        colKodeTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kodeTransaksi));

        // Tipe dengan warna
        colTipeTx.setCellValueFactory(d -> new SimpleStringProperty(
            "masuk".equals(d.getValue().tipe) ? "▲ Masuk" : "▼ Keluar"));
        colTipeTx.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setText(null); setStyle(""); return; }
                setText(s);
                setStyle(s.contains("Masuk")
                    ? "-fx-text-fill: #10b981; -fx-font-weight: bold;"
                    : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        });

        colNamaBarang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaBarang));
        colSupplierTx.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaSupplier));
        colJumlahTx.setCellValueFactory(d -> new SimpleStringProperty(
            String.valueOf(d.getValue().jumlah)));
        colTanggal.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tanggal));

        tableTransaksi.setItems(txList);
    }

    // ── FXML Handlers ─────────────────────────────────────────────────────────
    @FXML
    private void handleTipeChange() {
        String tipe = cbTipe.getValue();
        boolean isMasuk = "Barang Masuk".equals(tipe);
        // Supplier hanya tampil untuk Barang Masuk
        boxSupplier.setVisible(isMasuk);
        boxSupplier.setManaged(isMasuk);
    }

    @FXML
    private void handleBarangChange() {
        BarangDAO.Barang selected = cbBarang.getValue();
        if (selected != null) {
            lblInfoStok.setText("Stok saat ini: " + selected.stok + " " + selected.satuan);
            boxInfoStok.setVisible(true);
            boxInfoStok.setManaged(true);
        } else {
            boxInfoStok.setVisible(false);
            boxInfoStok.setManaged(false);
        }
    }

    @FXML
    private void handleSimpan() {
        // ── Validasi input ────────────────────────────────────────────────────
        String tipe = cbTipe.getValue();
        BarangDAO.Barang barang = cbBarang.getValue();
        String jumlahStr = tfJumlah.getText().trim();

        if (tipe == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih tipe transaksi terlebih dahulu!");
            return;
        }
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

        boolean isMasuk = "Barang Masuk".equals(tipe);

        // Cek stok cukup untuk keluar
        if (!isMasuk && barang.stok < jumlah) {
            showAlert(Alert.AlertType.WARNING, "Stok Tidak Cukup",
                "Stok " + barang.namaBarang + " saat ini hanya " + barang.stok + " " + barang.satuan + ".\n"
                + "Tidak bisa mengeluarkan " + jumlah + " " + barang.satuan + ".");
            return;
        }

        // ── Simpan transaksi ──────────────────────────────────────────────────
        SupplierDAO.Supplier supplier = cbSupplier.getValue();
        Integer supplierId = (supplier != null) ? supplier.id : null;
        String keterangan = taKeterangan.getText().trim();
        int userId = (currentUser != null) ? currentUser.id : 1;

        boolean ok = transaksiDAO.addTransaksi(
            isMasuk ? "masuk" : "keluar",
            barang.id, supplierId, null,
            jumlah, keterangan, userId
        );

        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Transaksi Berhasil",
                "✅ " + tipe + " berhasil dicatat!\n"
                + "Barang : " + barang.namaBarang + "\n"
                + "Jumlah : " + jumlah + " " + barang.satuan);
            clearForm();
            loadTransaksi();
            // Refresh data barang (stok sudah berubah)
            barangItems = barangDAO.getAllBarang();
            cbBarang.setItems(FXCollections.observableArrayList(barangItems));
            boxInfoStok.setVisible(false);
            boxInfoStok.setManaged(false);
        } else {
            showAlert(Alert.AlertType.ERROR, "Transaksi Gagal",
                "Transaksi gagal disimpan.\nPeriksa koneksi database atau stok barang.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadTransaksi();
        barangItems = barangDAO.getAllBarang();
        cbBarang.setItems(FXCollections.observableArrayList(barangItems));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void loadTransaksi() {
        txList.setAll(transaksiDAO.getAllTransaksi());
        lblTotalTx.setText(txList.size() + " transaksi");
    }

    private void clearForm() {
        cbTipe.setValue(null);
        cbBarang.setValue(null);
        cbSupplier.setValue(null);
        tfJumlah.clear();
        taKeterangan.clear();
        boxSupplier.setVisible(false);
        boxSupplier.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
