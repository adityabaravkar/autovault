package com.autovault.services;

import com.autovault.models.Doctor;
import com.autovault.models.ExcelData;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public interface MainService {
    List<Doctor> loadDoctors() throws SQLException;
    List<ExcelData> loadExcelData(List<Doctor> doctors) throws SQLException;
    List<ExcelData> saveExcelData(File file) throws IOException;
    void saveDoctor(Doctor doctor);
    void updateExcelData(ExcelData excelData, List<Doctor> doctors);
}
