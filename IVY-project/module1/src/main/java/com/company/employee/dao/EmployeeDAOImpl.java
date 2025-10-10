package com.company.employee.dao;

import com.company.employee.model.Employee;
import com.company.employee.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployeeDAOImpl implements EmployeeDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeDAOImpl.class);
    
    private static final String INSERT_EMPLOYEE = 
        "INSERT INTO employees (first_name, last_name, email, phone_number, hire_date, department, salary) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_EMPLOYEE_BY_ID = 
        "SELECT * FROM employees WHERE id = ?";
    
    private static final String SELECT_ALL_EMPLOYEES = 
        "SELECT * FROM employees ORDER BY last_name, first_name";
    
    private static final String SELECT_EMPLOYEES_BY_DEPARTMENT = 
        "SELECT * FROM employees WHERE department = ? ORDER BY last_name, first_name";
    
    private static final String UPDATE_EMPLOYEE = 
        "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone_number = ?, " +
        "hire_date = ?, department = ?, salary = ? WHERE id = ?";
    
    private static final String DELETE_EMPLOYEE = 
        "DELETE FROM employees WHERE id = ?";
    
    private static final String COUNT_EMPLOYEES = 
        "SELECT COUNT(*) FROM employees";
    
    @Override
    public Long save(Employee employee) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_EMPLOYEE, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getEmail());
            stmt.setString(4, employee.getPhoneNumber());
            stmt.setDate(5, Date.valueOf(employee.getHireDate()));
            stmt.setString(6, employee.getDepartment());
            stmt.setDouble(7, employee.getSalary());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        employee.setId(id);
                        logger.info("Employee saved successfully with ID: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving employee: {}", e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public Employee findById(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EMPLOYEE_BY_ID)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractEmployeeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding employee by ID {}: {}", id, e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public List<Employee> findAll() {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_EMPLOYEES);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                employees.add(extractEmployeeFromResultSet(rs));
            }
            logger.info("Found {} employees", employees.size());
        } catch (SQLException e) {
            logger.error("Error finding all employees: {}", e.getMessage(), e);
        }
        return employees;
    }
    
    @Override
    public List<Employee> findByDepartment(String department) {
        List<Employee> employees = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_EMPLOYEES_BY_DEPARTMENT)) {
            
            stmt.setString(1, department);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(extractEmployeeFromResultSet(rs));
                }
            }
            logger.info("Found {} employees in department: {}", employees.size(), department);
        } catch (SQLException e) {
            logger.error("Error finding employees by department {}: {}", department, e.getMessage(), e);
        }
        return employees;
    }
    
    @Override
    public boolean update(Employee employee) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_EMPLOYEE)) {
            
            stmt.setString(1, employee.getFirstName());
            stmt.setString(2, employee.getLastName());
            stmt.setString(3, employee.getEmail());
            stmt.setString(4, employee.getPhoneNumber());
            stmt.setDate(5, Date.valueOf(employee.getHireDate()));
            stmt.setString(6, employee.getDepartment());
            stmt.setDouble(7, employee.getSalary());
            stmt.setLong(8, employee.getId());
            
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            if (success) {
                logger.info("Employee updated successfully: {}", employee.getId());
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error updating employee {}: {}", employee.getId(), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean delete(Long id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_EMPLOYEE)) {
            
            stmt.setLong(1, id);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            if (success) {
                logger.info("Employee deleted successfully: {}", id);
            }
            return success;
        } catch (SQLException e) {
            logger.error("Error deleting employee {}: {}", id, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public int count() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_EMPLOYEES);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting employees: {}", e.getMessage(), e);
        }
        return 0;
    }
    
    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setId(rs.getLong("id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setPhoneNumber(rs.getString("phone_number"));
        
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }
        
        employee.setDepartment(rs.getString("department"));
        employee.setSalary(rs.getDouble("salary"));
        
        return employee;
    }
}