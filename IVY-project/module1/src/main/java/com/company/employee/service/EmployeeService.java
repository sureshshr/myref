package com.company.employee.service;

import com.company.employee.dao.EmployeeDAO;
import com.company.employee.dao.EmployeeDAOImpl;
import com.company.employee.model.Employee;

import java.time.LocalDate;
import java.util.List;

public class EmployeeService {
    
    private final EmployeeDAO employeeDAO;
    
    public EmployeeService() {
        this.employeeDAO = new EmployeeDAOImpl();
    }
    
    // Constructor for dependency injection (useful for testing)
    public EmployeeService(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }
    
    /**
     * Create a new employee
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param phoneNumber Phone number
     * @param hireDate Hire date
     * @param department Department
     * @param salary Salary
     * @return Created employee with ID, or null if creation failed
     */
    public Employee createEmployee(String firstName, String lastName, String email,
                                 String phoneNumber, LocalDate hireDate, String department, Double salary) {
        
        // Basic validation
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (email == null || !isValidEmail(email)) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (salary != null && salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        
        Employee employee = new Employee(firstName.trim(), lastName.trim(), email.trim(),
                                       phoneNumber != null ? phoneNumber.trim() : null,
                                       hireDate, department != null ? department.trim() : null, salary);
        
        Long id = employeeDAO.save(employee);
        return id != null ? employee : null;
    }
    
    /**
     * Find employee by ID
     * @param id Employee ID
     * @return Employee or null if not found
     */
    public Employee getEmployeeById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid employee ID is required");
        }
        return employeeDAO.findById(id);
    }
    
    /**
     * Get all employees
     * @return List of all employees
     */
    public List<Employee> getAllEmployees() {
        return employeeDAO.findAll();
    }
    
    /**
     * Get employees by department
     * @param department Department name
     * @return List of employees in the department
     */
    public List<Employee> getEmployeesByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required");
        }
        return employeeDAO.findByDepartment(department.trim());
    }
    
    /**
     * Update employee information
     * @param employee Employee with updated information
     * @return true if update successful, false otherwise
     */
    public boolean updateEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee with valid ID is required");
        }
        
        // Validate updated data
        if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (employee.getLastName() == null || employee.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (employee.getEmail() == null || !isValidEmail(employee.getEmail())) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (employee.getSalary() != null && employee.getSalary() < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
        
        return employeeDAO.update(employee);
    }
    
    /**
     * Delete employee by ID
     * @param id Employee ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteEmployee(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Valid employee ID is required");
        }
        return employeeDAO.delete(id);
    }
    
    /**
     * Get total number of employees
     * @return Total count
     */
    public int getTotalEmployeeCount() {
        return employeeDAO.count();
    }
    
    /**
     * Give salary raise to employee
     * @param employeeId Employee ID
     * @param raisePercentage Percentage increase (e.g., 10 for 10%)
     * @return true if raise applied successfully
     */
    public boolean giveSalaryRaise(Long employeeId, double raisePercentage) {
        if (employeeId == null || employeeId <= 0) {
            throw new IllegalArgumentException("Valid employee ID is required");
        }
        if (raisePercentage < 0) {
            throw new IllegalArgumentException("Raise percentage cannot be negative");
        }
        
        Employee employee = employeeDAO.findById(employeeId);
        if (employee == null) {
            return false;
        }
        
        if (employee.getSalary() != null) {
            double newSalary = employee.getSalary() * (1 + raisePercentage / 100);
            employee.setSalary(newSalary);
            return employeeDAO.update(employee);
        }
        
        return false;
    }
    
    /**
     * Simple email validation
     * @param email Email to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }
}