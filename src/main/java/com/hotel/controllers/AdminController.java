package com.hotel.controllers;

import com.hotel.models.*;
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
import javafx.scene.chart.*;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // === Dashboard Tab ===
    @FXML private Label totalRoomsValue, occupiedValue, availableValue;
    @FXML private Label occupancyValue, revenueValue, bookingsCountValue;
    @FXML private BarChart<String, Number> revenueBarChart;
    @FXML private PieChart revenueChart;
    @FXML private Label clockLabel;
    @FXML private Label seasonLabel;

    // === Add Room Tab ===
    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField priceField;
    @FXML private Label statusLabel;

    // === Room Inventory Tab ===
    @FXML private TabPane tabPane;
    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> colRoomNo;
    @FXML private TableColumn<Room, String> colRoomType;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, String> colStatus;
    @FXML private TableColumn<Room, String> colAmenities;
    @FXML private TextField roomSearchField;

    // === Bookings Tab ===
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> colBookingId, colGuestName, colRoomTypeB, colBookingStatus;
    @FXML private TableColumn<Booking, Integer> colBookedRoom, colDays;
    @FXML private TableColumn<Booking, Double> colAmount;
    @FXML private TextField bookingSearchField;

    // === Guest Lookup Tab ===
    @FXML private TextField guestSearchField;
    @FXML private TableView<Booking> guestResultsTable;
    @FXML private TableColumn<Booking, String> colGLBookingId, colGLGuestName, colGLContact;
    @FXML private TableColumn<Booking, String> colGLRoomType, colGLStatus;
    @FXML private TableColumn<Booking, Integer> colGLRoomNo, colGLDays;
    @FXML private TableColumn<Booking, Double> colGLAmount;

    // Internal filtered lists
    private ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private FilteredList<Room> filteredRooms;
    private ObservableList<Booking> allBookings = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredBookings;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupRoomsTable();
        setupBookingsTable();
        setupGuestLookupTable();
        setupRoomSearch();
        setupBookingSearch();
        setupClock();
        setupAnimatedTabs();
        addTooltips();
        updateSeasonLabel();
        refreshAll();

        // Keyboard shortcuts — deferred until scene is available
        tabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) setupKeyboardShortcuts(newScene);
        });
    }

    // ===================== TABLE SETUP =====================

    private void setupRoomsTable() {
        colRoomNo.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getRoomNumber()).asObject());
        colRoomType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoomType()));
        colPrice.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPricePerNight()).asObject());
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colAmenities.setCellValueFactory(d -> new SimpleStringProperty(String.join(", ", d.getValue().getAmenitiesList())));

        // Status badge cell factory
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Available":
                            setStyle("-fx-text-fill: #6bae59; -fx-font-weight: bold;");
                            break;
                        case "Occupied":
                            setStyle("-fx-text-fill: #cf6659; -fx-font-weight: bold;");
                            break;
                        case "Maintenance":
                            setStyle("-fx-text-fill: #d4a94e; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Price formatting
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("\u20B9%.2f", price));
            }
        });
    }

    private void setupBookingsTable() {
        colBookingId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookingId()));
        colGuestName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGuest().getName()));
        colBookedRoom.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getRoom().getRoomNumber()).asObject());
        colRoomTypeB.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoom().getRoomType()));
        colDays.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDaysStayed()).asObject());
        colAmount.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalAmount()).asObject());
        colBookingStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isCancelled() ? "Cancelled" : "Active"));

        // Booking status badge
        colBookingStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("Cancelled".equals(status)
                            ? "-fx-text-fill: #cf6659; -fx-font-weight: bold;"
                            : "-fx-text-fill: #6bae59; -fx-font-weight: bold;");
                }
            }
        });

        // Amount formatting
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("\u20B9%.2f", amount));
            }
        });
    }

    private void setupGuestLookupTable() {
        colGLBookingId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBookingId()));
        colGLGuestName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGuest().getName()));
        colGLContact.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGuest().getContactNumber()));
        colGLRoomNo.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getRoom().getRoomNumber()).asObject());
        colGLRoomType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRoom().getRoomType()));
        colGLDays.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDaysStayed()).asObject());
        colGLAmount.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalAmount()).asObject());
        colGLStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isCancelled() ? "Cancelled" : "Active"));

        colGLStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); }
                else {
                    setText(status);
                    setStyle("Cancelled".equals(status)
                            ? "-fx-text-fill: #cf6659; -fx-font-weight: bold;"
                            : "-fx-text-fill: #6bae59; -fx-font-weight: bold;");
                }
            }
        });

        colGLAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? null : String.format("\u20B9%.2f", amount));
            }
        });
    }

    // ===================== SEARCH / FILTER =====================

    private void setupRoomSearch() {
        filteredRooms = new FilteredList<>(allRooms, p -> true);
        roomSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredRooms.setPredicate(room -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String q = newVal.toLowerCase();
                return String.valueOf(room.getRoomNumber()).contains(q)
                        || room.getRoomType().toLowerCase().contains(q)
                        || room.getStatus().toLowerCase().contains(q);
            });
        });
        SortedList<Room> sorted = new SortedList<>(filteredRooms);
        sorted.comparatorProperty().bind(roomsTable.comparatorProperty());
        roomsTable.setItems(sorted);
    }

    private void setupBookingSearch() {
        filteredBookings = new FilteredList<>(allBookings, p -> true);
        bookingSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredBookings.setPredicate(b -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String q = newVal.toLowerCase();
                return b.getBookingId().toLowerCase().contains(q)
                        || b.getGuest().getName().toLowerCase().contains(q)
                        || String.valueOf(b.getRoom().getRoomNumber()).contains(q)
                        || b.getRoom().getRoomType().toLowerCase().contains(q);
            });
        });
        SortedList<Booking> sorted = new SortedList<>(filteredBookings);
        sorted.comparatorProperty().bind(bookingsTable.comparatorProperty());
        bookingsTable.setItems(sorted);
    }

    // ===================== CLOCK =====================

    private void setupClock() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy  HH:mm:ss");
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                clockLabel.setText(LocalDateTime.now().format(fmt))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        clockLabel.setText(LocalDateTime.now().format(fmt));
    }

    // ===================== ANIMATED TABS =====================

    private void setupAnimatedTabs() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() != null) {
                FadeTransition fade = new FadeTransition(Duration.millis(250), newTab.getContent());
                fade.setFromValue(0.4);
                fade.setToValue(1.0);
                fade.play();
            }
        });
    }

    // ===================== KEYBOARD SHORTCUTS =====================

    private void setupKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                () -> tabPane.getSelectionModel().select(1) // Add Room tab
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                this::refreshAll
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN),
                this::handleLogout
        );
    }

    // ===================== TOOLTIPS =====================

    private void addTooltips() {
        roomNumberField.setTooltip(new Tooltip("Enter a unique integer room number"));
        priceField.setTooltip(new Tooltip("Enter base price per night in Rupees"));
    }

    private void updateSeasonLabel() {
        if (seasonLabel != null)
            seasonLabel.setText("Current: " + SeasonalPricing.getSeasonLabel(LocalDate.now()));
    }

    // ===================== EVENT HANDLERS =====================

    @FXML
    public void handleAddRoom() {
        try {
            int roomNum = Integer.parseInt(roomNumberField.getText());
            String category = roomTypeCombo.getValue();
            double price = Double.parseDouble(priceField.getText());

            if (category == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Selection", "Please select a room category.");
                return;
            }

            boolean exists = HotelManager.getInstance().getRooms().stream()
                    .anyMatch(r -> r.getRoomNumber() == roomNum);
            if (exists) {
                showAlert(Alert.AlertType.ERROR, "Duplicate Room", "Room " + roomNum + " already exists.");
                return;
            }

            Room newRoom;
            switch (category) {
                case "Deluxe":  newRoom = new DeluxeRoom(roomNum, price); break;
                case "Luxury":  newRoom = new LuxuryRoom(roomNum, price); break;
                default:        newRoom = new StandardRoom(roomNum, price); break;
            }

            HotelManager.getInstance().addRoom(newRoom);
            roomNumberField.clear();
            priceField.clear();
            refreshAll();
            showToast(category + " Room " + roomNum + " added at \u20B9" + String.format("%.2f", price) + "/night");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Room number and price must be valid numbers.");
        }
    }

    @FXML
    public void handleEditRoomPrice() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a room from the table first.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.format("%.2f", selected.getPricePerNight()));
        dialog.setTitle("Edit Room Price");
        dialog.setHeaderText("Room " + selected.getRoomNumber() + " (" + selected.getRoomType() + ")");
        dialog.setContentText("New price per night (\u20B9):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(priceStr -> {
            try {
                double newPrice = Double.parseDouble(priceStr);
                if (newPrice <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Price", "Price must be positive.");
                    return;
                }
                selected.setPricePerNight(newPrice);
                HotelManager.getInstance().saveState();
                refreshAll();
                showToast("Room " + selected.getRoomNumber() + " price updated to \u20B9" + String.format("%.2f", newPrice));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Enter a valid number.");
            }
        });
    }

    @FXML
    public void handleToggleMaintenance() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a room first.");
            return;
        }
        if (!selected.isAvailable() && !selected.isUnderMaintenance()) {
            showAlert(Alert.AlertType.ERROR, "Room Occupied", "Cannot set maintenance — room is occupied. Checkout first.");
            return;
        }
        HotelManager.getInstance().toggleMaintenance(selected);
        refreshAll();
        showToast("Room " + selected.getRoomNumber() + " " +
                (selected.isUnderMaintenance() ? "set to maintenance" : "back online"));
    }

    @FXML
    public void handleDeleteRoom() {
        Room selected = roomsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a room from the table first.");
            return;
        }
        if (!selected.isAvailable() && !selected.isUnderMaintenance()) {
            showAlert(Alert.AlertType.ERROR, "Room Occupied",
                    "Cannot delete Room " + selected.getRoomNumber() + " \u2014 it is currently occupied.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Room " + selected.getRoomNumber() + "?");
        confirm.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            HotelManager.getInstance().getRooms().remove(selected);
            HotelManager.getInstance().saveState();
            refreshAll();
            showToast("Room " + selected.getRoomNumber() + " deleted");
        }
    }

    @FXML
    public void handleCancelBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a booking first.");
            return;
        }
        if (selected.isCancelled()) {
            showAlert(Alert.AlertType.INFORMATION, "Already Cancelled", "This booking is already cancelled.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Cancel booking " + selected.getBookingId() + "?");
        confirm.setContentText("Guest: " + selected.getGuest().getName() +
                "\nRoom: " + selected.getRoom().getRoomNumber());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            HotelManager.getInstance().cancelBooking(selected);
            refreshAll();
            showToast("Booking " + selected.getBookingId() + " cancelled");
        }
    }

    @FXML
    public void handleGuestSearch() {
        String query = guestSearchField.getText();
        if (query == null || query.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Search", "Enter a guest name to search.");
            return;
        }
        List<Booking> results = HotelManager.getInstance().searchByGuestName(query);
        guestResultsTable.setItems(FXCollections.observableArrayList(results));
        showToast(results.size() + " booking(s) found for \"" + query + "\"");
    }

    @FXML public void handleRefresh()         { refreshAll(); }
    @FXML public void handleRefreshRooms()    { refreshRoomsTable(); }
    @FXML public void handleRefreshBookings() { refreshBookingsTable(); }

    @FXML
    public void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) tabPane.getScene().getWindow();
            LoginController.loadLoginScreen(stage);
        }
    }

    @FXML
    public void handleAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("About");
        about.setHeaderText("Hotel Management System v2.0");
        about.setContentText(
            "A JavaFX-based hotel management application.\n\n" +
            "Features:\n" +
            "\u2022 Room inventory with edit, maintenance & delete\n" +
            "\u2022 Booking, checkout & cancellation\n" +
            "\u2022 Revenue analytics (Bar + Pie charts)\n" +
            "\u2022 Guest history lookup\n" +
            "\u2022 Seasonal dynamic pricing\n" +
            "\u2022 Real-time search & filtering\n" +
            "\u2022 Data persistence via file serialization\n\n" +
            "Built with JavaFX 17 + Maven");
        about.showAndWait();
    }

    @FXML
    public void handleExportRooms() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Room Data");
        fc.setInitialFileName("rooms_export.txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fc.showSaveDialog(tabPane.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(String.format("%-10s | %-12s | %-12s | %-12s | %-30s\n",
                        "Room No.", "Type", "Price/Night", "Status", "Amenities"));
                writer.write("=".repeat(80) + "\n");
                for (Room r : HotelManager.getInstance().getRooms()) {
                    writer.write(String.format("%-10d | %-12s | \u20B9%-11.2f | %-12s | %-30s\n",
                            r.getRoomNumber(), r.getRoomType(), r.getPricePerNight(),
                            r.getStatus(),
                            String.join(", ", r.getAmenitiesList())));
                }
                showToast("Data exported to: " + file.getName());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", e.getMessage());
            }
        }
    }

    // ===================== REFRESH HELPERS =====================

    private void refreshAll() {
        refreshRoomsTable();
        refreshBookingsTable();
        refreshSummaryCards();
        refreshBarChart();
        refreshPieChart();
    }

    private void refreshRoomsTable() {
        allRooms.setAll(HotelManager.getInstance().getRooms());
    }

    private void refreshBookingsTable() {
        allBookings.setAll(HotelManager.getInstance().getBookings());
    }

    private void refreshSummaryCards() {
        HotelManager hm = HotelManager.getInstance();
        int total = hm.getRooms().size();
        long occupied = hm.getRooms().stream().filter(r -> !r.isAvailable() && !r.isUnderMaintenance()).count();
        long available = hm.getRooms().stream().filter(r -> r.isAvailable() && !r.isUnderMaintenance()).count();

        totalRoomsValue.setText(String.valueOf(total));
        occupiedValue.setText(String.valueOf(occupied));
        availableValue.setText(String.valueOf(available));
        occupancyValue.setText(String.format("%.1f%%", hm.getOccupancyRate()));
        revenueValue.setText("\u20B9" + String.format("%.0f", hm.getTotalRevenue()));
        bookingsCountValue.setText(String.valueOf(hm.getActiveBookingsCount()));
    }

    private void refreshBarChart() {
        List<Booking> bookings = HotelManager.getInstance().getBookings();

        double stdRev = bookings.stream().filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomType().equals("Standard"))
                .mapToDouble(Booking::getTotalAmount).sum();
        double dlxRev = bookings.stream().filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomType().equals("Deluxe"))
                .mapToDouble(Booking::getTotalAmount).sum();
        double luxRev = bookings.stream().filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomType().equals("Luxury"))
                .mapToDouble(Booking::getTotalAmount).sum();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        series.getData().add(new XYChart.Data<>("Standard", stdRev));
        series.getData().add(new XYChart.Data<>("Deluxe", dlxRev));
        series.getData().add(new XYChart.Data<>("Luxury", luxRev));

        revenueBarChart.getData().clear();
        revenueBarChart.getData().add(series);
        revenueBarChart.setLegendVisible(false);
    }

    private void refreshPieChart() {
        List<Booking> bookings = HotelManager.getInstance().getBookings();

        double stdRev = bookings.stream().filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomType().equals("Standard"))
                .mapToDouble(Booking::getTotalAmount).sum();
        double dlxRev = bookings.stream().filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomType().equals("Deluxe"))
                .mapToDouble(Booking::getTotalAmount).sum();
        double luxRev = bookings.stream().filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomType().equals("Luxury"))
                .mapToDouble(Booking::getTotalAmount).sum();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        if (stdRev > 0) data.add(new PieChart.Data("Standard (\u20B9" + String.format("%.0f", stdRev) + ")", stdRev));
        if (dlxRev > 0) data.add(new PieChart.Data("Deluxe (\u20B9" + String.format("%.0f", dlxRev) + ")", dlxRev));
        if (luxRev > 0) data.add(new PieChart.Data("Luxury (\u20B9" + String.format("%.0f", luxRev) + ")", luxRev));
        if (data.isEmpty()) data.add(new PieChart.Data("No bookings yet", 1));

        revenueChart.setData(data);
    }

    // ===================== TOAST NOTIFICATION =====================

    private void showToast(String message) {
        try {
            Label toast = new Label(message);
            toast.getStyleClass().add("toast");

            Popup popup = new Popup();
            popup.getContent().add(toast);
            popup.setAutoHide(true);

            Stage stage = (Stage) tabPane.getScene().getWindow();
            // Position at bottom-center of stage
            popup.show(stage,
                    stage.getX() + (stage.getWidth() / 2) - 120,
                    stage.getY() + stage.getHeight() - 70);

            // Fade out after 2.5 seconds, then hide
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), toast);
            fadeOut.setDelay(Duration.seconds(2.5));
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> popup.hide());
            fadeOut.play();
        } catch (Exception ignored) {
            // Fallback: silently ignore if popup can't be shown
        }
    }

    // ===================== ALERT HELPER =====================

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}