package com.autovault.repositories;

import com.autovault.models.Doctor;

import java.sql.SQLException;
import java.util.List;

public interface DoctorRepository {
    List<Doctor> getAllDoctors() throws SQLException;
    void addDoctor(Doctor doctor);
}
