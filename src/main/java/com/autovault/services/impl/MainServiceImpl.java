package com.autovault.services.impl;

import com.autovault.models.Doctor;
import com.autovault.models.ExcelData;
import com.autovault.repositories.DoctorRepository;
import com.autovault.repositories.ExcelDataRepository;
import com.autovault.repositories.impl.DoctorRepositoryImpl;
import com.autovault.repositories.impl.ExcelDataRepositoryImpl;
import com.autovault.services.MainService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.autovault.utils.Util.getCellValue;

public class MainServiceImpl implements MainService {

    private final ExcelDataRepository excelDataRepository;

    private final DoctorRepository doctorRepository;

    public MainServiceImpl() throws SQLException {
        excelDataRepository = new ExcelDataRepositoryImpl();
        doctorRepository = new DoctorRepositoryImpl();
    }

    @Override
    public List<Doctor> loadDoctors() throws SQLException {
        return doctorRepository.getAllDoctors();
    }

    @Override
    public List<ExcelData> loadExcelData(List<Doctor> doctors) throws SQLException {
        return excelDataRepository.getAllExcelData(doctors);
    }

    @Override
    public List<ExcelData> saveExcelData(File file) throws IOException {
        List<ExcelData> dataList = new ArrayList<>();
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
            excelDataRepository.addExcelData(data);
            dataList.add(data);
        }

        workbook.close();
        inputStream.close();

        return dataList;
    }

    @Override
    public void saveDoctor(Doctor doctor) {
        doctorRepository.addDoctor(doctor);
    }

    @Override
    public void updateExcelData(ExcelData excelData, List<Doctor> doctors) {
        excelDataRepository.updateExcelData(excelData, doctors);
    }
}
