package com.autovault.repositories.impl;

import com.autovault.config.Database;
import com.autovault.models.Doctor;
import com.autovault.repositories.DoctorRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorRepositoryImpl implements Database, DoctorRepository {

    private static final Logger logger = LogManager.getLogger(DoctorRepositoryImpl.class);

    public DoctorRepositoryImpl() throws SQLException {
        try(Connection connection = connection()) {
            connection.createStatement().executeUpdate(
                    "CREATE TABLE IF NOT EXISTS doctors (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "name TEXT NOT NULL," +
                            "address TEXT NOT NULL" +
                            ")"
            );
        }
    }

    @Override
    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctorList = new ArrayList<>();
        try(Connection connection = connection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM doctors");
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String address = resultSet.getString("address");
                Doctor doctor = new Doctor(id, name, address);
                doctorList.add(doctor);
            }
        }
        return doctorList;
    }

    @Override
    public void addDoctor(Doctor doctor) {
        try(Connection connection = connection()) {
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
}
