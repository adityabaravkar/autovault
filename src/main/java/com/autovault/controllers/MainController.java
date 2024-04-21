package com.autovault.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.autovault.models.Doctor;
import com.autovault.models.ExcelData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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

    private Connection connection;

    public void initialize() {
        initializeDatabase();
        createTablesIfNotExist();
        loadData();
        setupTableColumns();
        setupDoctorsTableView();
    }

    private void initializeDatabase() {
        try {
            // Connect to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:mydatabase.db");
            logger.info("Database connection established.");
        } catch (SQLException e) {
            logger.error("Failed to initialize database", e);
        }
    }
    private void createTablesIfNotExist() {
        try {
            // Create the "doctors" table if it doesn't exist
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS doctors (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "address TEXT NOT NULL" +
                            ")"
            );

            // Create the "excel_data" table if it doesn't exist
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS excel_data (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "column1 TEXT," +
                            "column2 TEXT," +
                            "doctor_id INTEGER," +
                            "FOREIGN KEY (doctor_id) REFERENCES doctors(id)" +
                            ")"
            );
        } catch (SQLException e) {
            logger.error("Failed to create tables in db", e);
        }
    }

    private void loadData() {
        try {
            // Load doctors from the database
            doctors.addAll(loadDoctorsFromDatabase());

            // Load Excel data from the database
            excelData.addAll(loadExcelDataFromDatabase());
        } catch (SQLException e) {
            logger.error("Failed to load data", e);
        }
    }

    private List<Doctor> loadDoctorsFromDatabase() throws SQLException {
        List<Doctor> doctorList = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM doctors");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            String address = resultSet.getString("address");
            Doctor doctor = new Doctor(id, name, address);
            doctorList.add(doctor);
        }

        return doctorList;
    }

    private List<ExcelData> loadExcelDataFromDatabase() throws SQLException {
        List<ExcelData> dataList = new ArrayList<>();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM excel_data");
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String column1 = resultSet.getString("column1");
            String column2 = resultSet.getString("column2");
            int doctorId = resultSet.getInt("doctor_id");
            Doctor doctor = findDoctorById(doctorId);
            ExcelData data = new ExcelData(id, column1, column2, doctor);
            dataList.add(data);
        }

        return dataList;
    }

    private Doctor findDoctorById(int id) {
        for (Doctor doctor : doctors) {
            if (doctor.getId() == id) {
                return doctor;
            }
        }
        return null;
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
                loadExcelData(selectedFile);
            } catch (IOException e) {
                logger.error("Failed to load excel data", e);
            }
        }
    }

    private void loadExcelData(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                // Skip the header row
                continue;
            }

            String column1 = getCellValue(row.getCell(0));
            String column2 = getCellValue(row.getCell(1));

            ExcelData data = new ExcelData(column1, column2, null);
            excelData.add(data);
            saveExcelDataToDatabase(data);
        }

        workbook.close();
        inputStream.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private void saveExcelDataToDatabase(ExcelData data) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO excel_data (column1, column2, doctor_id) VALUES (?, ?, ?)");
            statement.setString(1, data.getColumn1());
            statement.setString(2, data.getColumn2());
            statement.setInt(3, data.getDoctor() != null ? data.getDoctor().getId() : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to save excel data to db", e);
        }
    }

    @FXML
    private void addDoctor() {
        String name = doctorNameField.getText();
        String address = doctorAddressField.getText();

        if (!name.isEmpty() && !address.isEmpty()) {
            Doctor doctor = new Doctor(name, address);
            doctors.add(doctor);
            saveDoctorToDatabase(doctor);
            doctorNameField.clear();
            doctorAddressField.clear();
        }
    }

    private void saveDoctorToDatabase(Doctor doctor) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO doctors (name, address) VALUES (?, ?)");
            statement.setString(1, doctor.getName());
            statement.setString(2, doctor.getAddress());
            statement.executeUpdate();

            // Get the generated doctor ID
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                int doctorId = resultSet.getInt(1);
                doctor.setId(doctorId);
            }
        } catch (SQLException e) {
            logger.error("Failed to save doctor data to db", e);
        }
    }

    private void setupTableColumns() {
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
            updateExcelDataInDatabase(data);
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

    private void updateExcelDataInDatabase(ExcelData data) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE excel_data SET doctor_id = ? WHERE id = ?");
            statement.setInt(1, data.getDoctor() != null ? data.getDoctor().getId() : 0);
            statement.setInt(2, data.getId());
            statement.executeUpdate();

            int doctorId = data.getDoctor() != null ? data.getDoctor().getId() : 0;
            data.setDoctor(findDoctorById(doctorId));
        } catch (SQLException e) {
            logger.error("Failed to update excel data", e);
        }
    }


    private static class DoctorStringConverter extends javafx.util.StringConverter<Doctor> {
        @Override
        public String toString(Doctor object) {
            return object == null ? "" : object.getName();
        }

        @Override
        public Doctor fromString(String string) {
            // Not needed for this example
            return null;
        }
    }
}