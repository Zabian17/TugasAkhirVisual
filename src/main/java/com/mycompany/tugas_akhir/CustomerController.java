package com.mycompany.tugas_akhir;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller untuk halaman Customer.
 * Menampilkan daftar customer dalam bentuk card grid dengan fitur filter dan search.
 */
public class CustomerController implements Initializable {

    @FXML private ComboBox<String> cbCategory;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfSearch;
    @FXML private TilePane tileCustomers;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private List<CustomerDAO.Customer> allCustomers;
    private List<CustomerDAO.Customer> filteredCustomers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFilters();
        loadCustomers();
    }

    public void initUser(UserDAO.User user) {
        // User context available for future use if needed
    }

    private void setupFilters() {
        cbCategory.getItems().addAll("All");
        cbCategory.setValue("All");

        cbStatus.getItems().addAll("All status", "Aktif", "Nonaktif");
        cbStatus.setValue("All status");
    }

    private void loadCustomers() {
        allCustomers = customerDAO.getAllCustomer();
        applyFilters();
    }

    private void applyFilters() {
        filteredCustomers = allCustomers;

        // Filter by status
        String selectedStatus = cbStatus.getValue();
        if (selectedStatus != null && !selectedStatus.equals("All status")) {
            filteredCustomers = filteredCustomers.stream()
                .filter(c -> selectedStatus.equals(c.status))
                .toList();
        }

        // Filter by search term
        String searchTerm = tfSearch.getText().trim().toLowerCase();
        if (!searchTerm.isEmpty()) {
            filteredCustomers = filteredCustomers.stream()
                .filter(c -> c.namaCustomer.toLowerCase().contains(searchTerm) ||
                           c.posisi.toLowerCase().contains(searchTerm) ||
                           c.alamat.toLowerCase().contains(searchTerm) ||
                           c.kontak.toLowerCase().contains(searchTerm))
                .toList();
        }

        displayCustomers(filteredCustomers);
    }

    private void displayCustomers(List<CustomerDAO.Customer> customers) {
        tileCustomers.getChildren().clear();

        if (customers.isEmpty()) {
            Label emptyLabel = new Label("Belum ada data customer");
            emptyLabel.setStyle("-fx-text-fill: #9ba3b8; -fx-font-size: 14px;");
            tileCustomers.getChildren().add(emptyLabel);
            return;
        }

        for (CustomerDAO.Customer customer : customers) {
            tileCustomers.getChildren().add(createCustomerCard(customer));
        }
    }

    private VBox createCustomerCard(CustomerDAO.Customer customer) {
        VBox card = new VBox(12);
        card.setStyle("-fx-border-color: #e5e7eb; -fx-border-radius: 8; " +
                      "-fx-background-color: #ffffff; -fx-padding: 20; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
        card.setPrefWidth(300);

        // Company Name
        Label lblName = new Label(customer.namaCustomer);
        lblName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        // Position
        HBox positionBox = new HBox(8);
        positionBox.setAlignment(Pos.CENTER_LEFT);
        Label iconPos = new Label("👤");
        Label lblPosition = new Label(customer.posisi);
        lblPosition.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ba3b8;");
        positionBox.getChildren().addAll(iconPos, lblPosition);

        // Address (without icon)
        Label lblAddress = new Label(customer.alamat);
        lblAddress.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ba3b8;");
        lblAddress.setWrapText(true);

        // Phone (without icon)
        Label lblPhone = new Label(customer.kontak);
        lblPhone.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ba3b8;");

        // Status badge
        Label lblStatus = new Label(customer.status);
        String statusColor = "Aktif".equals(customer.status) ? "#10b981" : "#ef4444";
        lblStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-padding: 4 8; " +
                          "-fx-background-color: " + statusColor + "; -fx-background-radius: 4;");

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnEdit = new Button("Edit");
        btnEdit.setStyle("-fx-font-size: 12px; -fx-padding: 6 16; -fx-background-color: #818cf8; " +
                        "-fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> handleEditCustomer(customer));

        Button btnContact = new Button("Contact");
        btnContact.setStyle("-fx-font-size: 12px; -fx-padding: 6 16; -fx-background-color: #3b82f6; " +
                          "-fx-text-fill: white; -fx-background-radius: 4; -fx-cursor: hand;");
        btnContact.setOnAction(e -> handleContact(customer));

        buttonBox.getChildren().addAll(btnEdit, btnContact);

        // Add all elements to card
        card.getChildren().addAll(
            lblName,
            lblStatus,
            positionBox,
            lblAddress,
            lblPhone,
            new Separator(),
            buttonBox
        );

        return card;
    }

    @FXML private void handleAddCustomer() {
        Dialog<CustomerDAO.Customer> dialog = new Dialog<>();
        dialog.setTitle("Tambah Customer Baru");
        dialog.setHeaderText("Masukkan data customer baru");

        TextField tfName = new TextField();
        tfName.setPromptText("Nama Perusahaan");
        TextField tfPosition = new TextField();
        tfPosition.setPromptText("Posisi/Jabatan");
        TextField tfAddress = new TextField();
        tfAddress.setPromptText("Alamat");
        TextField tfPhone = new TextField();
        tfPhone.setPromptText("No. Telepon");
        TextField tfEmail = new TextField();
        tfEmail.setPromptText("Email");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        content.getChildren().addAll(
            new Label("Nama Perusahaan:"), tfName,
            new Label("Posisi:"), tfPosition,
            new Label("Alamat:"), tfAddress,
            new Label("No. Telepon:"), tfPhone,
            new Label("Email:"), tfEmail
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = tfName.getText().trim();
                if (name.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama perusahaan tidak boleh kosong!");
                    return null;
                }

                if (customerDAO.addCustomer(name, tfPosition.getText(), tfAddress.getText(),
                                           tfPhone.getText(), tfEmail.getText())) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "✅ Customer berhasil ditambahkan!");
                    loadCustomers();
                    return new CustomerDAO.Customer(0, name, tfPosition.getText(), tfAddress.getText(),
                                                    tfPhone.getText(), tfEmail.getText(), "Aktif");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Customer gagal ditambahkan.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML private void handleEditCustomer(CustomerDAO.Customer customer) {
        Dialog<CustomerDAO.Customer> dialog = new Dialog<>();
        dialog.setTitle("Edit Customer");
        dialog.setHeaderText("Edit data customer");

        TextField tfName = new TextField(customer.namaCustomer);
        TextField tfPosition = new TextField(customer.posisi);
        TextField tfAddress = new TextField(customer.alamat);
        TextField tfPhone = new TextField(customer.kontak);
        TextField tfEmail = new TextField(customer.email);
        ComboBox<String> cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Aktif", "Nonaktif");
        cbStatus.setValue(customer.status);

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        content.getChildren().addAll(
            new Label("Nama Perusahaan:"), tfName,
            new Label("Posisi:"), tfPosition,
            new Label("Alamat:"), tfAddress,
            new Label("No. Telepon:"), tfPhone,
            new Label("Email:"), tfEmail,
            new Label("Status:"), cbStatus
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                if (customerDAO.updateCustomer(customer.id, tfName.getText(), tfPosition.getText(),
                                              tfAddress.getText(), tfPhone.getText(),
                                              tfEmail.getText(), cbStatus.getValue())) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "✅ Customer berhasil diperbarui!");
                    loadCustomers();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Customer gagal diperbarui.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleContact(CustomerDAO.Customer customer) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kontak Customer");
        alert.setHeaderText(customer.namaCustomer);
        alert.setContentText("Telepon: " + customer.kontak + "\nEmail: " + customer.email);
        alert.showAndWait();
    }

    @FXML private void handleFilterChange() {
        applyFilters();
    }

    @FXML private void handleSearch() {
        applyFilters();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
