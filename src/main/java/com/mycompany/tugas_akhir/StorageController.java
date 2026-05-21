package com.mycompany.tugas_akhir;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Controller untuk halaman Storage (Manajemen Barang).
 * Fitur: Tampil daftar barang, Tambah, Edit, Hapus, Search real-time.
 */
public class StorageController implements Initializable {

    private final BarangDAO barangDAO = new BarangDAO();
    private final ObservableList<BarangDAO.Barang> barangList = FXCollections.observableArrayList();

    // ── FXML Refs ─────────────────────────────────────────────────────────────
    @FXML private TextField  searchField;
    @FXML private Label      lblTotalBarang;
    @FXML private Label      lblStokRendah;
    @FXML private Label      lblStokHabis;

    @FXML private TableView<BarangDAO.Barang>             tableBarang;
    @FXML private TableColumn<BarangDAO.Barang, Number>   colNo;
    @FXML private TableColumn<BarangDAO.Barang, String>   colKode;
    @FXML private TableColumn<BarangDAO.Barang, String>   colNama;
    @FXML private TableColumn<BarangDAO.Barang, String>   colKategori;
    @FXML private TableColumn<BarangDAO.Barang, String>   colSatuan;
    @FXML private TableColumn<BarangDAO.Barang, Number>   colStok;
    @FXML private TableColumn<BarangDAO.Barang, Number>   colStokMin;
    @FXML private TableColumn<BarangDAO.Barang, String>   colStatus;
    @FXML private TableColumn<BarangDAO.Barang, Void>     colAksi;

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
    }

    // ── Setup Kolom ───────────────────────────────────────────────────────────
    private void setupColumns() {
        // Nomor urut
        colNo.setCellValueFactory(data ->
            new SimpleIntegerProperty(tableBarang.getItems().indexOf(data.getValue()) + 1));
        colNo.setSortable(false);

        colKode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kodeBarang));
        colNama.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().namaBarang));
        colKategori.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kategori));
        colSatuan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().satuan));
        colStok.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().stok));
        colStokMin.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().stokMin));

        // Stok – merah jika di bawah minimum
        colStok.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setStyle(""); return; }
                BarangDAO.Barang b = getTableView().getItems().get(getIndex());
                setText(n.toString());
                setStyle(b.stok < b.stokMin
                    ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;"
                    : "-fx-text-fill: #374151;");
            }
        });

        // Kolom Status dengan badge berwarna
        colStatus.setCellValueFactory(d -> {
            BarangDAO.Barang b = d.getValue();
            if (b.stok <= 0)            return new SimpleStringProperty("Habis");
            if (b.stok < b.stokMin)     return new SimpleStringProperty("Kritis");
            if (b.stok < b.stokMin * 2) return new SimpleStringProperty("Rendah");
            return new SimpleStringProperty("Aman");
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().setAll("badge",
                    switch (s) {
                        case "Habis", "Kritis" -> "badge-danger";
                        case "Rendah"          -> "badge-warn";
                        default                -> "badge-ok";
                    });
                setGraphic(badge);
                setText(null);
            }
        });

        // Kolom Aksi – tombol Edit dan Hapus
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏ Edit");
            private final Button btnDel  = new Button("🗑 Hapus");
            private final HBox   box     = new HBox(6, btnEdit, btnDel);
            {
                btnEdit.getStyleClass().addAll("btn-small");
                btnDel.getStyleClass().addAll("btn-small", "btn-danger-small");
                box.setAlignment(Pos.CENTER_LEFT);
                btnEdit.setOnAction(e -> showBarangDialog(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(e  -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableBarang.setItems(barangList);
    }

    // ── Load Data ─────────────────────────────────────────────────────────────
    private void loadData() {
        barangList.setAll(barangDAO.getAllBarang());
        updateStats();
    }

    private void updateStats() {
        long total  = barangList.size();
        long habis  = barangList.stream().filter(b -> b.stok <= 0).count();
        long rendah = barangList.stream().filter(b -> b.stok > 0 && b.stok < b.stokMin).count();
        lblTotalBarang.setText(String.valueOf(total));
        lblStokHabis.setText(String.valueOf(habis));
        lblStokRendah.setText(String.valueOf(rendah));
    }

    // ── FXML Handlers ─────────────────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim();
        barangList.setAll(kw.isEmpty() ? barangDAO.getAllBarang() : barangDAO.searchBarang(kw));
        updateStats();
    }

    @FXML
    private void handleTambah() {
        showBarangDialog(null);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    private void handleDelete(BarangDAO.Barang b) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Barang");
        confirm.setHeaderText(null);
        confirm.setContentText("Hapus barang \"" + b.namaBarang + "\" (" + b.kodeBarang + ")?\n"
            + "Pastikan tidak ada transaksi yang terkait dengan barang ini.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (barangDAO.deleteBarang(b.id)) {
                    loadData();
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Barang berhasil dihapus.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal",
                        "Tidak bisa menghapus barang.\nBarang mungkin masih memiliki riwayat transaksi.");
                }
            }
        });
    }

    // ── Dialog Tambah / Edit ──────────────────────────────────────────────────
    private void showBarangDialog(BarangDAO.Barang existing) {
        boolean isEdit = existing != null;

        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(isEdit ? "Edit Barang" : "Tambah Barang Baru");
        dlg.getDialogPane().getStylesheets().add(
            getClass().getResource("/css/dashboard.css").toExternalForm());

        // Grid form
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(20, 28, 20, 28));
        grid.setMinWidth(440);

        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints(230, 230, Double.MAX_VALUE);
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        // Fields
        TextField tfKode = styledField(isEdit ? existing.kodeBarang : barangDAO.generateKodeBarang());
        tfKode.setEditable(!isEdit);
        if (isEdit) tfKode.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #9ba3b8; "
                                  + "-fx-background-radius: 8; -fx-border-radius: 8;");

        TextField tfNama     = styledField(isEdit ? existing.namaBarang : "");
        tfNama.setPromptText("Nama barang...");

        TextField tfKategori = styledField(isEdit ? existing.kategori : "");
        tfKategori.setPromptText("Umum, Elektronik, dll.");

        TextField tfSatuan   = styledField(isEdit ? existing.satuan : "pcs");

        TextField tfStok     = styledField(isEdit ? String.valueOf(existing.stok) : "0");
        tfStok.setPromptText("0");

        TextField tfStokMin  = styledField(isEdit ? String.valueOf(existing.stokMin) : "0");
        tfStokMin.setPromptText("0");

        addRow(grid, 0, "Kode Barang",              tfKode);
        addRow(grid, 1, "Nama Barang *",             tfNama);
        addRow(grid, 2, "Kategori",                  tfKategori);
        addRow(grid, 3, "Satuan",                    tfSatuan);
        addRow(grid, 4, isEdit ? "Stok Saat Ini" : "Stok Awal",  tfStok);
        addRow(grid, 5, "Stok Min. (Alert)",         tfStokMin);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style tombol OK
        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText(isEdit ? "💾  Simpan" : "➕  Tambah");
        okBtn.getStyleClass().add("btn-primary");

        // Handle result
        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String nama = tfNama.getText().trim();
            String kode = tfKode.getText().trim();
            if (nama.isEmpty() || kode.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Kode dan Nama barang wajib diisi!");
                return;
            }
            int stok, stokMin;
            try {
                stok    = Integer.parseInt(tfStok.getText().trim());
                stokMin = Integer.parseInt(tfStokMin.getText().trim());
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Nilai stok harus berupa angka!");
                return;
            }

            boolean ok = isEdit
                ? barangDAO.updateBarang(existing.id, nama, tfKategori.getText().trim(),
                                         tfSatuan.getText().trim(), stok, stokMin)
                : barangDAO.addBarang(kode, nama, tfKategori.getText().trim(),
                                      tfSatuan.getText().trim(), stok, stokMin);

            if (ok) {
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                    isEdit ? "Data barang berhasil diperbarui." : "Barang baru berhasil ditambahkan.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal",
                    isEdit ? "Gagal memperbarui data barang."
                           : "Gagal menambah barang. Kode barang mungkin sudah digunakan.");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private TextField styledField(String value) {
        TextField tf = new TextField(value);
        tf.getStyleClass().add("form-field");
        return tf;
    }

    private void addRow(GridPane grid, int row, String labelText, TextField field) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        lbl.setMinWidth(130);
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
