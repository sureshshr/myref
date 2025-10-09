package com.example.module2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final String url;
    private final String user;
    private final String password;

    public StudentDAO(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public List<Student> listStudents() throws Exception {
        List<Student> result = new ArrayList<>();
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        try (Connection con = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = con.prepareStatement("SELECT id, name, major FROM Student");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(new Student(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        return result;
    }
}
