package com.hotel.controllers;

import com.hotel.models.Booking;
import com.hotel.models.Guest;
import com.hotel.models.Room;
import com.hotel.utils.BillingService;
import com.hotel.utils.HotelManager;
import com.hotel.utils.SeasonalPricing;
import javafx.animation.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class UserController implements Initializable {

    // Search filters
    @FXML private ComboBox<String> typeFilter;
    @FXML private TextField priceFilter;
    @FXML private Label statusLabel;

    // Booking fields
    @FXML private TextField guestNameField;
    @FXML private TextField guestContactField;
    @FXML private TextField roomToBookField;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;

    // Room TableView
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> colRoomNo;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, String> colAmenities;

    // Header
    @FXML private Label clockLabel;
    @FXML private Label seasonInfoLabel;
    @FXML private TextField roomSearchField;

    private String lastInvoice = "";
    private ObservableList<Room> allAvailableRooms = FXCollections.observableArrayList();
    private FilteredList<Room> filteredAvailable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (typeFilter != null) typeFilter.setValue("All");

        checkInPicker.setValue(LocalDate.now());
        checkOutPicker.setValue(LocalDate.now().plusDays(1));

        setupTable();
        setupSearch();
        setupClock();
        addTooltips();
        updateSeasonInfo();
        handleViewAvailableRooms();

        // Keyboard shortcuts
        roomsTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) setupKeyboardShortcuts(newScene);
        });
    }

    private void setupTable() {
        colRoomNo.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getRoomNumber()).asObject());
        colRoomType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomType()));
        colPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPricePerNight()).asObject());
        colAmenities.setCellValueFactory(d -> new SimpleStringProperty(String.join(", ", d.getValue().getAmenitiesList())));

        // Price formatting
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("\u20B9%.2f", price));
            }
        });

        // Click table row to auto-fill room number
        roomsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) roomToBookField.setText(String.valueOf(sel.getRoomNumber()));
        });
    }

    private void setupSearch() {
        filteredAvailable = new FilteredList<>(allAvailableRooms, p -> true);
        if (roomSearchField != null) {
            roomSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredAvailable.setPredicate(room -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String q = newVal.toLowerCase();
                    return String.valueOf(room.getRoomNumber()).contains(q)
                            || room.getRoomType().toLowerCase().contains(q)
                            || String.join(", ", room.getAmenitiesList()).toLowerCase().contains(q);
                });
            });
        }
        SortedList<Room> sorted = new SortedList<>(filteredAvailable);
        sorted.comparatorProperty().bind(roomsTable.comparatorProperty());
        roomsTable.setItems(sorted);
    }

    private void setupClock() {
        if (clockLabel == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                clockLabel.setText(LocalDateTime.now().format(fmt))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }

    private void setupKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN),
                this::handleBooking
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
                this::handleLogout
        );
    }

    private void addTooltips() {
        guestNameField.setTooltip(new Tooltip("Enter the full name of the guest"));
        guestContactField.setTooltip(new Tooltip("Enter phone number or email"));
        roomToBookField.setTooltip(new Tooltip("Enter room number or click a row in the table"));
        checkInPicker.setTooltip(new Tooltip("Select the check-in date"));
        checkOutPicker.setTooltip(new Tooltip("Select the check-out date"));
    }

    private void updateSeasonInfo() {
        if (seasonInfoLabel != null)
            seasonInfoLabel.setText(SeasonalPricing.getSeasonLabel(LocalDate.now()));
    }

    @FXML
    public void handleViewAvailableRooms() {
        String type = typeFilter.getValue();
        double maxPrice = 0;
        try {
            String txt = priceFilter.getText();
            if (txt != null && !txt.trim().isEmpty()) maxPrice = Double.parseDouble(txt);
        } catch (NumberFormatException ignored) {}

        List<Room> rooms = HotelManager.getInstance().filterRooms(type, maxPrice);
        allAvailableRooms.setAll(rooms);
        showStatus(rooms.size() + " room(s) found.", "-text-primary");
    }

    @FXML
    public void handleBooking() {
        try {
            String name = guestNameField.getText();
            String contact = guestContactField.getText();

            if (name == null || name.trim().isEmpty() || contact == null || contact.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Info", "Guest name and contact are required.");
                return;
            }

            String roomText = roomToBookField.getText();
            if (roomText == null || roomText.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Room", "Enter a room number or select from the table.");
                return;
            }
            int roomNum = Integer.parseInt(roomText);

            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            if (checkIn == null || checkOut == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Dates", "Select both check-in and check-out dates.");
                return;
            }
            if (!checkOut.isAfter(checkIn)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Dates", "Check-out must be after check-in.");
                return;
            }
            if (checkIn.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Invalid Dates", "Check-in cannot be in the past.");
                return;
            }

            int days = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

            Room selectedRoom = HotelManager.getInstance().getRooms().stream()
                    .filter(r -> r.getRoomNumber() == roomNum && r.isAvailable() && !r.isUnderMaintenance())
                    .findFirst().orElse(null);

            if (selectedRoom == null) {
                showAlert(Alert.AlertType.ERROR, "Room Unavailable",
                        "Room " + roomNum + " is not available or doesn't exist.");
                return;
            }

            // Overlap check
            if (HotelManager.getInstance().hasOverlappingBooking(roomNum, checkIn, checkOut)) {
                showAlert(Alert.AlertType.ERROR, "Date Conflict",
                        "Room " + roomNum + " has an overlapping booking for these dates.");
                return;
            }

            // Dynamic pricing
            double seasonMultiplier = SeasonalPricing.getMultiplier(checkIn);
            double estimated = selectedRoom.calculateTotalStayCost(days) * seasonMultiplier;

            // Confirmation dialog with season info
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Booking");
            confirm.setHeaderText("Book Room " + roomNum + " for " + name + "?");
            String seasonNote = seasonMultiplier > 1.0
                    ? "\nSeason: " + SeasonalPricing.getSeasonLabel(checkIn)
                    : "";
            confirm.setContentText(String.format(
                    "Type: %s\nDuration: %d night(s) (%s to %s)%s\nEstimated Total: \u20B9%.2f",
                    selectedRoom.getRoomType(), days, checkIn, checkOut, seasonNote, estimated));

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Guest guest = new Guest(UUID.randomUUID().toString().substring(0, 8), name, contact);
                Booking booking = new Booking(UUID.randomUUID().toString().substring(0, 8),
                        guest, selectedRoom, days, checkIn, checkOut, seasonMultiplier);

                HotelManager.getInstance().addBooking(booking);
                guest.addBookingToHistory(booking);

                guestNameField.clear();
                guestContactField.clear();
                roomToBookField.clear();
                checkInPicker.setValue(LocalDate.now());
                checkOutPicker.setValue(LocalDate.now().plusDays(1));
                handleViewAvailableRooms();
                showToast("Booking confirmed! ID: " + booking.getBookingId() +
                        " | Total: \u20B9" + String.format("%.2f", booking.getTotalAmount()));
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Room number must be a valid number.");
        }
    }

    @FXML
    public void handleCheckout() {
        try {
            String roomText = roomToBookField.getText();
            if (roomText == null || roomText.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Missing Room", "Enter the room number to checkout.");
                return;
            }
            int roomNum = Integer.parseInt(roomText);

            Booking activeBooking = HotelManager.getInstance().getBookings().stream()
                    .filter(b -> b.getRoom().getRoomNumber() == roomNum && !b.getRoom().isAvailable() && !b.isCancelled())
                    .findFirst().orElse(null);

            if (activeBooking == null) {
                showAlert(Alert.AlertType.ERROR, "No Booking", "No active booking for room " + roomNum);
                return;
            }

            lastInvoice = BillingService.generateInvoice(activeBooking);

            Alert invoiceAlert = new Alert(Alert.AlertType.INFORMATION);
            invoiceAlert.setTitle("Checkout Complete");
            invoiceAlert.setHeaderText("Room " + roomNum + " \u2014 Invoice");

            TextArea invoiceArea = new TextArea(lastInvoice);
            invoiceArea.setEditable(false);
            invoiceArea.setWrapText(true);
            invoiceArea.setPrefRowCount(18);
            invoiceAlert.getDialogPane().setExpandableContent(invoiceArea);
            invoiceAlert.getDialogPane().setExpanded(true);
            invoiceAlert.showAndWait();

            activeBooking.getRoom().setAvailable(true);
            HotelManager.getInstance().saveState();
            handleViewAvailableRooms();
            showToast("Checkout complete for Room " + roomNum);

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter a valid room number.");
        }
    }

    @FXML
    public void handleExportInvoice() {
        if (lastInvoice.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Invoice", "Perform a checkout first to generate an invoice.");
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Invoice");
        fc.setInitialFileName("invoice.txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fc.showSaveDialog(roomsTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(lastInvoice);
                showToast("Invoice saved to: " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) roomsTable.getScene().getWindow();
            LoginController.loadLoginScreen(stage);
        }
    }

    @FXML
    public void handleAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Hotel Management System v2.0");
        about.setContentText("Built with JavaFX 17 + Maven\n\nBook rooms, manage stays, and generate invoices.\n" +
                "Now with seasonal pricing, date validation, and real-time search.");
        about.showAndWait();
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    private void showToast(String message) {
        try {
            Label toast = new Label(message);
            toast.getStyleClass().add("toast");
            Popup popup = new Popup();
            popup.getContent().add(toast);
            popup.setAutoHide(true);
            Stage stage = (Stage) roomsTable.getScene().getWindow();
            popup.show(stage,
                    stage.getX() + (stage.getWidth() / 2) - 140,
                    stage.getY() + stage.getHeight() - 70);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
            fadeOut.setDelay(Duration.seconds(2.5));
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> popup.hide());
            fadeOut.play();
        } catch (Exception ignored) {}
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}