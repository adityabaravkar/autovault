package com.autovault.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.autovault.models.Doctor;
import com.autovault.models.ExcelData;

import com.autovault.services.MainService;
import com.autovault.services.impl.MainServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.util.Callback;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class MainController {

    private static final Logger logger = LogManager.getLogger(MainController.class);

    @FXML
    private TableView<ExcelData> excelDataTable;

    @FXML
    private TextField doctorNameField;

    @FXML
    private TextField doctorAddressField;

    @FXML
    private TableView<Doctor> doctorsTableView;

    @FXML
    private TableColumn<Doctor, Integer> doctorIdColumn;

    @FXML
    private TableColumn<Doctor, String> doctorNameColumn;

    @FXML
    private TableColumn<Doctor, String> doctorAddressColumn;

    private final ObservableList<ExcelData> excelData = FXCollections.observableArrayList();
    private final ObservableList<Doctor> doctors = FXCollections.observableArrayList();

    private MainService service;

    public void initialize() throws SQLException {
        service = new MainServiceImpl();
        doctors.addAll(service.loadDoctors());
        excelData.addAll(service.loadExcelData(doctors));
        setupExcelDataTableView();
        setupDoctorsTableView();
    }

    @FXML
    private void uploadFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Excel File");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));

        File selectedFile = fileChooser.showOpenDialog(excelDataTable.getScene().getWindow());
        if (selectedFile != null) {
            try {
                excelData.addAll(service.saveExcelData(selectedFile));
            } catch (IOException e) {
                logger.error("Failed to load excel data", e);
            }
        }
    }

    @FXML
    private void addDoctor() {
        String name = doctorNameField.getText();
        String address = doctorAddressField.getText();

        if (!name.isEmpty() && !address.isEmpty()) {
            Doctor doctor = new Doctor(name, address);
            doctors.add(doctor);
            service.saveDoctor(doctor);
            doctorNameField.clear();
            doctorAddressField.clear();
        }
    }

    private void setupExcelDataTableView() {
        TableColumn<ExcelData, String> column1 = new TableColumn<>("Column 1");
        column1.setCellValueFactory(new PropertyValueFactory<>("column1"));

        TableColumn<ExcelData, String> column2 = new TableColumn<>("Column 2");
        column2.setCellValueFactory(new PropertyValueFactory<>("column2"));

        column1.setPrefWidth(200);
        column2.setPrefWidth(200);

        Callback<TableColumn<ExcelData, Doctor>, TableCell<ExcelData, Doctor>> cellFactory =
                param -> {
                    ComboBoxTableCell<ExcelData, Doctor> cell = new ComboBoxTableCell<>(FXCollections.observableArrayList(doctors));
                    cell.setConverter(new DoctorStringConverter());
                    cell.setFocusTraversable(true);
                    cell.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1 && !cell.isEmpty()) {
                            cell.getTableView().edit(cell.getIndex(), cell.getTableColumn());
                        }
                    });

                    return cell;
                };

        TableColumn<ExcelData, Doctor> doctorColumn = new TableColumn<>("Doctor");
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        doctorColumn.setCellFactory(cellFactory);
        doctorColumn.setOnEditCommit(event -> {
            ExcelData data = event.getRowValue();
            data.setDoctor(event.getNewValue());
            service.updateExcelData(data, doctors);
        });

        excelDataTable.getColumns().addAll(column1, column2, doctorColumn);
        excelDataTable.setItems(excelData);
        excelDataTable.setEditable(true);
        excelDataTable.getSelectionModel().setCellSelectionEnabled(true);
    }

    private void setupDoctorsTableView() {
        doctorIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        doctorNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        doctorAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));

        doctorIdColumn.setPrefWidth(20);
        doctorNameColumn.setPrefWidth(200);
        doctorAddressColumn.setPrefWidth(200);

        doctorsTableView.setItems(doctors);
    }

    private static class DoctorStringConverter extends javafx.util.StringConverter<Doctor> {
        @Override
        public String toString(Doctor object) {
            return object == null ? "" : object.getName();
        }

        @Override
        public Doctor fromString(String string) {
            // Not needed
            return null;
        }
    }
}