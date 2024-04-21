package com.autovault.repositories.impl;

import com.autovault.config.Database;
import com.autovault.models.Doctor;
import com.autovault.models.ExcelData;
import com.autovault.repositories.ExcelDataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.autovault.utils.Util.findDoctorById;

public class ExcelDataRepositoryImpl implements Database, ExcelDataRepository {

    private static final Logger logger = LogManager.getLogger(ExcelDataRepositoryImpl.class);

    public ExcelDataRepositoryImpl() throws SQLException {
        try(Connection connection = connection()) {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS excel_data (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "column1 TEXT," +
                            "column2 TEXT," +
                            "doctor_id INTEGER," +
                            "FOREIGN KEY (doctor_id) REFERENCES doctors(id)" +
                            ")"
            );
        }
    }

    @Override
    public List<ExcelData> getAllExcelData(List<Doctor> doctors) throws SQLException {
        List<ExcelData> dataList = new ArrayList<>();
        try(Connection connection = connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM excel_data");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String column1 = resultSet.getString("column1");
                String column2 = resultSet.getString("column2");
                int doctorId = resultSet.getInt("doctor_id");
                Doctor doctor = findDoctorById(doctorId, doctors);
                ExcelData data = new ExcelData(id, column1, column2, doctor);
                dataList.add(data);
            }
        }
        return dataList;
    }

    @Override
    public void addExcelData(ExcelData excelData) {
        try(Connection connection = connection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO excel_data (column1, column2, doctor_id) VALUES (?, ?, ?)");
            statement.setString(1, excelData.getColumn1());
            statement.setString(2, excelData.getColumn2());
            statement.setInt(3, excelData.getDoctor() != null ? excelData.getDoctor().getId() : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to save excel data to db", e);
        }
    }

    @Override
    public void updateExcelData(ExcelData excelData, List<Doctor> doctors) {
        try(Connection connection = connection()) {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE excel_data SET doctor_id = ? WHERE id = ?");
            statement.setInt(1, excelData.getDoctor() != null ? excelData.getDoctor().getId() : 0);
            statement.setInt(2, excelData.getId());
            statement.executeUpdate();

            int doctorId = excelData.getDoctor() != null ? excelData.getDoctor().getId() : 0;
            excelData.setDoctor(findDoctorById(doctorId, doctors));
        } catch (SQLException e) {
            logger.error("Failed to update excel data", e);
        }
    }
}
