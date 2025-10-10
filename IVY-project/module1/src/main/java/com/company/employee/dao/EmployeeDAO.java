package com.company.employee.dao;

import com.company.employee.model.Employee;
import java.util.List;

public interface EmployeeDAO {
    
    /**
     * Save a new employee to the database
     * @param employee Employee object to save
     * @return Generated employee ID
     */
    Long save(Employee employee);
    
    /**
     * Find employee by ID
     * @param id Employee ID
     * @return Employee object or null if not found
     */
    Employee findById(Long id);
    
    /**
     * Find all employees
     * @return List of all employees
     */
    List<Employee> findAll();
    
    /**
     * Find employees by department
     * @param department Department name
     * @return List of employees in the department
     */
    List<Employee> findByDepartment(String department);
    
    /**
     * Update existing employee
     * @param employee Employee object with updated data
     * @return true if update successful, false otherwise
     */
    boolean update(Employee employee);
    
    /**
     * Delete employee by ID
     * @param id Employee ID to delete
     * @return true if deletion successful, false otherwise
     */
    boolean delete(Long id);
    
    /**
     * Count total number of employees
     * @return Total count of employees
     */
    int count();
}