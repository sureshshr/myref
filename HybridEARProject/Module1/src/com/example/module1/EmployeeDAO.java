package com.example.module1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private final String url;
    private final String user;
    private final String password;

    public EmployeeDAO(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public List<Employee> listEmployees() throws Exception {
        List<Employee> result = new ArrayList<>();
        // Load driver is optional for newer JDBC, but keep for Java 8 compatibility
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement("SELECT id, name, title FROM Employee");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Employee(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return result;
    }
}
