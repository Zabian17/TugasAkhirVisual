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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

/**
 * Controller untuk halaman Storage (Manajemen Barang).
 * Fitur: Tampil daftar barang, Tambah, Edit, Hapus, Search real-time.
 */
public class StorageController implements Initializable {

    private final BarangDAO barangDAO = new BarangDAO();
    private final ObservableList<BarangDAO.Barang> barangList = FXCollections.observableArrayList();

    // ── FXML Refs ─────────────────────────────────────────────────────────────
    @FXML private TextField  searchField;
    @FXML private ComboBox<String> comboFilter1;
    @FXML private ComboBox<String> comboStatusFilter;
    @FXML private ComboBox<String> comboCategoryFilter;
    
    @FXML private Label      lblTotalBarang;
    @FXML private Label      lblStokSehat;
    @FXML private Label      lblStokRendah;
    @FXML private Label      lblStokHabis;

    @FXML private TableView<BarangDAO.Barang>             tableBarang;
    @FXML private TableColumn<BarangDAO.Barang, Number>   colNo;
    @FXML private TableColumn<BarangDAO.Barang, String>   colKode;
    @FXML private TableColumn<BarangDAO.Barang, String>   colKategori;
    @FXML private TableColumn<BarangDAO.Barang, String>   colLokasi;
    @FXML private TableColumn<BarangDAO.Barang, Number>   colStok;
    @FXML private TableColumn<BarangDAO.Barang, String>   colStokMin;
    @FXML private TableColumn<BarangDAO.Barang, String>   colStatus;
    @FXML private TableColumn<BarangDAO.Barang, String>   colLastUpdate;

    // ── Lifecycle ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeFilters();
        setupColumns();
        loadData();
    }
    
    private void initializeFilters() {
        comboFilter1.setItems(FXCollections.observableArrayList("All"));
        comboFilter1.setValue("All");
        
        comboStatusFilter.setItems(FXCollections.observableArrayList(
            "All status", "In Stock", "Low Stock", "No Stock"));
        comboStatusFilter.setValue("All status");
        
        comboCategoryFilter.setItems(FXCollections.observableArrayList("All categories"));
        comboCategoryFilter.setValue("All categories");
    }

    private void setupColumns() {
        // Nomor urut
        colNo.setCellValueFactory(data ->
            new SimpleIntegerProperty(tableBarang.getItems().indexOf(data.getValue()) + 1));
        colNo.setSortable(false);

        // Kode + Nama dalam satu kolom
        colKode.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().kodeBarang + " • " + d.getValue().namaBarang));

        colKategori.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().kategori));
        colKategori.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().setAll("badge", "badge-info");
                badge.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 11px;");
                setGraphic(badge);
                setText(null);
            }
        });

        colLokasi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().lokasi));

        colStok.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().stok));
        colStok.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setStyle(""); return; }
                BarangDAO.Barang b = getTableView().getItems().get(getIndex());
                setText(n.toString());
                if (b.stok <= 0)
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                else if (b.stok < b.stokMin)
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                else
                    setStyle("-fx-text-fill: #10b981;");
            }
        });

        // Min / Max dalam satu kolom
        colStokMin.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().stokMin + " / " + d.getValue().stokMax));

        // Kolom Status dengan 3 kategori: In Stock, Low Stock, No Stock
        colStatus.setCellValueFactory(d -> {
            BarangDAO.Barang b = d.getValue();
            if (b.stok <= 0)                return new SimpleStringProperty("No Stock");
            if (b.stok < b.stokMin)         return new SimpleStringProperty("Low Stock");
            return new SimpleStringProperty("In Stock");
        });
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label badge = new Label(s);
                badge.getStyleClass().setAll("badge",
                    switch (s) {
                        case "No Stock"  -> "badge-danger";
                        case "Low Stock" -> "badge-warn";
                        default          -> "badge-ok";
                    });
                setGraphic(badge);
                setText(null);
            }
        });

        // Last Update - format timestamp
        colLastUpdate.setCellValueFactory(d -> {
            long timestamp = d.getValue().lastUpdate;
            String formatted = formatLastUpdate(timestamp);
            return new SimpleStringProperty(formatted);
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
        long sehat  = barangList.stream().filter(b -> b.stok > 0 && b.stok >= b.stokMin).count();
        long rendah = barangList.stream().filter(b -> b.stok > 0 && b.stok < b.stokMin).count();
        long habis  = barangList.stream().filter(b -> b.stok <= 0).count();
        lblTotalBarang.setText(String.valueOf(total));
        lblStokSehat.setText(String.valueOf(sehat));
        lblStokRendah.setText(String.valueOf(rendah));
        lblStokHabis.setText(String.valueOf(habis));
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

    @FXML
    private void handleExport() {
        if (barangList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Export Gagal", "Tidak ada data barang untuk diekspor.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan File PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Inventory_" + System.currentTimeMillis() + ".pdf");
        
        java.io.File selectedFile = fileChooser.showSaveDialog(tableBarang.getScene().getWindow());
        if (selectedFile != null) {
            try {
                exportToPDF(selectedFile);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", 
                    "File PDF berhasil disimpan di:\n" + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error Export", 
                    "Gagal mengekspor ke PDF:\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToPDF(java.io.File file) throws Exception {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
        document.open();
        
        // Header
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("Laporan Inventaris Barang");
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        title.getFont().setSize(18);
        title.getFont().setStyle(com.itextpdf.text.Font.BOLD);
        document.add(title);
        
        com.itextpdf.text.Paragraph date = new com.itextpdf.text.Paragraph(
            "Tanggal: " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")));
        date.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        date.getFont().setSize(10);
        date.setSpacingAfter(12);
        document.add(date);
        
        // Stats
        long sehat = barangList.stream().filter(b -> b.stok > 0 && b.stok >= b.stokMin).count();
        long rendah = barangList.stream().filter(b -> b.stok > 0 && b.stok < b.stokMin).count();
        long habis = barangList.stream().filter(b -> b.stok <= 0).count();
        
        com.itextpdf.text.Paragraph stats = new com.itextpdf.text.Paragraph(
            "Total Barang: " + barangList.size() + " | In Stock: " + sehat + " | Low Stock: " + rendah + " | Out of Stock: " + habis);
        stats.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        stats.getFont().setSize(10);
        stats.setSpacingAfter(16);
        document.add(stats);
        
        // Table
        com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(8);
        table.setWidthPercentage(100);
        
        // Header cells
        String[] headers = {"#", "ITEM / SKU", "CATEGORY", "LOCATION", "QTY", "MIN/MAX", "STATUS", "LAST UPDATE"};
        for (String h : headers) {
            com.itextpdf.text.pdf.PdfPCell cell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph(h));
            cell.setHorizontalAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        
        // Data rows
        int no = 1;
        for (BarangDAO.Barang b : barangList) {
            String status = b.stok <= 0 ? "No Stock" : (b.stok < b.stokMin ? "Low Stock" : "In Stock");
            
            table.addCell(String.valueOf(no++));
            table.addCell(b.kodeBarang + " • " + b.namaBarang);
            table.addCell(b.kategori);
            table.addCell(b.lokasi);
            table.addCell(String.valueOf(b.stok));
            table.addCell(b.stokMin + " / " + b.stokMax);
            table.addCell(status);
            table.addCell(formatLastUpdate(b.lastUpdate));
        }
        
        document.add(table);
        document.close();
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

    // ── Format Last Update Time ────────────────────────────────────────────────
    private String formatLastUpdate(long timestamp) {
        if (timestamp == 0) return "-";
        long now = System.currentTimeMillis();
        long diffMs = now - timestamp;
        long diffSec = diffMs / 1000;
        long diffMin = diffSec / 60;
        long diffHour = diffMin / 60;
        long diffDay = diffHour / 24;

        if (diffSec < 60) return "Just now";
        if (diffMin < 60) return diffMin + " min ago";
        if (diffHour < 24) return diffHour + " h ago";
        if (diffDay < 7) return diffDay + " d ago";
        
        java.time.LocalDateTime ldt = java.time.LocalDateTime
            .ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
        return ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
}
