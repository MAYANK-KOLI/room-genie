package checkin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import hotel.Reservation;
import hotel.Room;
import project.DataBase;
import java.net.URL;
import java.util.ResourceBundle;

public class ConfirmnDataController implements Initializable {

    @FXML private TextField NameField, PhoneField, EmailField, CityField, NationalityField, PassportField, CardNumField, CVCcodeField, roomIDField;
    @FXML private TextArea AddressField;
    @FXML private Label Room_Type, CheckOutLabel, Room_Capacity, Number_of_Nights, Night_Cost, CheckInLabel, Total_fees;

    private static final double GST_RATE_LOW = 0.12; // 12% GST
    private static final double GST_RATE_HIGH = 0.18; // 18% GST

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Reservation reservation = CheckInController.reservation;
        
        // Populate fields with reservation data
        NameField.setText(reservation.getGuest().getName());
        PhoneField.setText(reservation.getGuest().getPhoneNo());
        EmailField.setText(reservation.getGuest().getEmail());
        AddressField.setText(reservation.getGuest().getAddress());
        CityField.setText(reservation.getGuest().getCity());
        NationalityField.setText(reservation.getGuest().getNationality());
        PassportField.setText(reservation.getGuest().getPassportNumber());
        CardNumField.setText(reservation.getGuest().getCardNumber());
        CVCcodeField.setText(reservation.getGuest().getCardPass());
        Room_Type.setText(reservation.getRoom().getRoom_Type());
        Room_Capacity.setText(reservation.getRoom().getRoom_capacity());
        roomIDField.setText(String.valueOf(reservation.getGuest().getRoomID()));
        CheckOutLabel.setText(CheckInController.Date_TO_LocalDate(reservation.getRoom().getCheck_Out_Date()).toString());
        CheckInLabel.setText(CheckInController.Date_TO_LocalDate(reservation.getRoom().getCheck_In_Date()).toString());
        Number_of_Nights.setText(String.valueOf(reservation.getGuest().getNumberOfDaysStayed()));
        Night_Cost.setText(String.valueOf(reservation.getRoom().nightCost(reservation.getRoom())));
        Total_fees.setText(reservation.getGuest().getFees() + " ₹");
    }

    @FXML
    private void SaveButtonAction(ActionEvent event) {
        DataBase db = new DataBase();
        Reservation reservation = CheckInController.reservation;
        
        Room.CheckIn(reservation.getGuest(), reservation.getRoom(), reservation.getGuest().getRoomID());
        
        printBill(NameField.getScene().getRoot(), reservation);
        
        new homepage.HomePageController().goTO_check_in(event);
        ((Stage) NameField.getScene().getWindow()).close();
    }

    @FXML
    private void CancelAction(ActionEvent event) {
        ((Stage) NameField.getScene().getWindow()).close();
    }

    private void printBill(Node node, Reservation reservation) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(node.getScene().getWindow())) {
            VBox content = new VBox(20);
            content.setAlignment(Pos.CENTER);

            Label hotelName = new Label("TMS HOTEL");
            hotelName.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

            Label customerName = new Label("Customer: " + reservation.getGuest().getName());
            customerName.setStyle("-fx-font-size: 18;");

            GridPane table = createBillTable(reservation);

            content.getChildren().addAll(hotelName, customerName, table);

            if (job.printPage(content)) {
                job.endJob();
                showAlert("Print Successful", "Bill has been printed successfully!", AlertType.INFORMATION);
            } else {
                showAlert("Print Failed", "Failed to print the bill.", AlertType.ERROR);
            }
        }
    }

    private GridPane createBillTable(Reservation reservation) {
        GridPane table = new GridPane();
        table.setAlignment(Pos.CENTER);
        table.setHgap(15);
        table.setVgap(15);
        table.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-padding: 10;");

        // Add table headers
        addTableRow(table, 0, "Description", "Amount", true);
        
        // Calculate costs
        double totalCost = calculateTotalCost(reservation);
        double gst = calculateGST(totalCost);
        double totalWithGST = totalCost + gst;

        // Add table content
        addTableRow(table, 1, "Room Type", reservation.getRoom().getRoom_Type(), false);
        addTableRow(table, 2, "Nights Stayed", String.valueOf(reservation.getGuest().getNumberOfDaysStayed()), false);
        addTableRow(table, 3, "Cost Per Night", formatCurrency(reservation.getRoom().nightCost(reservation.getRoom())), false);
        addTableRow(table, 4, "Total Room Cost", formatCurrency(totalCost), false);
        addTableRow(table, 5, "GST (" + (totalCost < 2500 ? "12%" : "18%") + ")", formatCurrency(gst), false);
        addTableRow(table, 6, "Total Amount", formatCurrency(totalWithGST), false);

        return table;
    }

    private void addTableRow(GridPane table, int row, String label, String value, boolean isHeader) {
        Label labelNode = new Label(label);
        Label valueNode = new Label(value);

        String headerStyle = "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-color: blue; -fx-padding: 10;";
        String valueStyle = "-fx-text-fill: black; -fx-padding: 10;";

        if (isHeader) {
            labelNode.setStyle(headerStyle);
            valueNode.setStyle(headerStyle);
        } else {
            labelNode.setStyle(valueStyle);
            valueNode.setStyle(valueStyle);
        }

        // Set min width for columns for better spacing
        labelNode.setMinWidth(150); // Minimum width for label column
        valueNode.setMinWidth(100); // Minimum width for value column

        table.add(labelNode, 0, row);
        table.add(valueNode, 1, row);
    }

    private String formatCurrency(double amount) {
        return String.format("%.2f ₹", amount);
    }

    private double calculateTotalCost(Reservation reservation) {
        return reservation.getRoom().nightCost(reservation.getRoom()) * reservation.getGuest().getNumberOfDaysStayed();
    }

    private double calculateGST(double totalCost) {
        return totalCost < 2500 ? totalCost * GST_RATE_LOW : totalCost * GST_RATE_HIGH;
    }

    private void showAlert(String title, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}