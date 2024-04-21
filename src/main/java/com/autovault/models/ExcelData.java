package com.autovault.models;

public class ExcelData {
    private int id;
    private String column1;
    private String column2;
    private Doctor doctor;

    public ExcelData(String column1, String column2, Doctor doctor) {
        this.column1 = column1;
        this.column2 = column2;
        this.doctor = doctor;
    }

    public ExcelData(int id, String column1, String column2, Doctor doctor) {
        this.id = id;
        this.column1 = column1;
        this.column2 = column2;
        this.doctor = doctor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
