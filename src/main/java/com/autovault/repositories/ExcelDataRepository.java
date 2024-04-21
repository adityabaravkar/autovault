package com.autovault.repositories;

import com.autovault.models.Doctor;
import com.autovault.models.ExcelData;

import java.sql.SQLException;
import java.util.List;

public interface ExcelDataRepository {
    List<ExcelData> getAllExcelData(List<Doctor> doctors) throws SQLException;
    void addExcelData(ExcelData excelData);
    void updateExcelData(ExcelData excelData,List<Doctor> doctors);
}
