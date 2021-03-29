package com.mindex.challenge.data;

import org.springframework.data.annotation.Id;

public class Compensation {

    @Id
    private String employeeId;
    private int salary;

    // Date format: MM/DD/YYYY
    private String effectiveDate;

    public Compensation(){
    }

    public String getEmployeeId(){
        return employeeId;
    }

    public void setEmployeeId(String employeeId){
        this.employeeId = employeeId;
    }

    public int getSalary(){
        return salary;
    }

    public void setSalary(int salary){
        this.salary = salary;
    }

    public String getEffectiveDate(){
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate){
        this.effectiveDate = effectiveDate;
    }
}
