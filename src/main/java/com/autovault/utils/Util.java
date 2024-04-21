package com.autovault.utils;

import com.autovault.models.Doctor;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

public class Util {

    private Util() {
        //Utility class
    }

    public static Doctor findDoctorById(int id, List<Doctor> doctors) {
        for (Doctor doctor : doctors) {
            if (doctor.getId() == id) {
                return doctor;
            }
        }
        return null;
    }

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

}
