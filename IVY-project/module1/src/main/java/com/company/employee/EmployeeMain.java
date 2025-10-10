package com.company.employee;

import com.company.employee.model.Employee;
import com.company.employee.service.EmployeeService;
import com.company.employee.util.DatabaseConnection;

import java.time.LocalDate;
import java.util.List;

public class EmployeeMain {
    
    public static void main(String[] args) {
        System.out.println("=== Employee Management System ===");
        
        EmployeeService employeeService = new EmployeeService();
        
        try {
            // Display connection pool info
            System.out.println("Database Connection: " + DatabaseConnection.getPoolInfo());
            
            // Create sample employees
            System.out.println("\n--- Creating Sample Employees ---");
            
            Employee emp1 = employeeService.createEmployee(
                "John", "Doe", "john.doe@company.com", 
                "555-1234", LocalDate.of(2020, 1, 15), 
                "Engineering", 75000.0
            );
            
            Employee emp2 = employeeService.createEmployee(
                "Jane", "Smith", "jane.smith@company.com", 
                "555-5678", LocalDate.of(2019, 5, 20), 
                "Marketing", 65000.0
            );
            
            Employee emp3 = employeeService.createEmployee(
                "Mike", "Johnson", "mike.johnson@company.com", 
                "555-9012", LocalDate.of(2021, 3, 10), 
                "Engineering", 80000.0
            );
            
            System.out.println("Created employees:");
            if (emp1 != null) System.out.println("- " + emp1);
            if (emp2 != null) System.out.println("- " + emp2);
            if (emp3 != null) System.out.println("- " + emp3);
            
            // Display all employees
            System.out.println("\n--- All Employees ---");
            List<Employee> allEmployees = employeeService.getAllEmployees();
            allEmployees.forEach(emp -> System.out.println(emp));
            
            // Display employees by department
            System.out.println("\n--- Engineering Department ---");
            List<Employee> engineeringEmployees = employeeService.getEmployeesByDepartment("Engineering");
            engineeringEmployees.forEach(emp -> System.out.println(emp));
            
            // Give salary raise
            if (emp1 != null) {
                System.out.println("\n--- Giving 10% raise to " + emp1.getFirstName() + " " + emp1.getLastName() + " ---");
                boolean raiseGiven = employeeService.giveSalaryRaise(emp1.getId(), 10.0);
                if (raiseGiven) {
                    Employee updatedEmp = employeeService.getEmployeeById(emp1.getId());
                    System.out.println("Updated employee: " + updatedEmp);
                }
            }
            
            // Display total count
            System.out.println("\n--- Employee Statistics ---");
            System.out.println("Total employees: " + employeeService.getTotalEmployeeCount());
            
        } catch (Exception e) {
            System.err.println("Error in employee management: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up database connections
            DatabaseConnection.closeDataSource();
            System.out.println("\nDatabase connections closed.");
        }
    }
}