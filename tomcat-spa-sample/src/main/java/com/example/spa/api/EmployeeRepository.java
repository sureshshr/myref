package com.example.spa.api;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmployeeRepository {
	private final DataSource dataSource;

	public EmployeeRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void initSchema() throws SQLException {
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(
				"CREATE TABLE IF NOT EXISTS employees (" +
						"id VARCHAR(36) PRIMARY KEY," +
						"name VARCHAR(200) NOT NULL," +
						"email VARCHAR(320) NOT NULL," +
						"department VARCHAR(40) NOT NULL," +
						"salary DECIMAL(12,2) NOT NULL," +
						"active BOOLEAN NOT NULL," +
						"created_at TIMESTAMP NOT NULL," +
						"updated_at TIMESTAMP NOT NULL" +
						")")) {
			stmt.execute();
		}
	}

	public void seedIfEmpty() throws SQLException {
		String countSql = "SELECT COUNT(*) AS c FROM employees";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(countSql)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next() && rs.getLong("c") > 0) {
					return;
				}
			}
		}

		EmployeeInput e1 = new EmployeeInput();
		e1.name = "Employee 1";
		e1.email = "employee1@example.invalid";
		e1.department = "Engineering";
		e1.salary = new BigDecimal("95000.00");
		e1.active = true;

		EmployeeInput e2 = new EmployeeInput();
		e2.name = "Employee 2";
		e2.email = "employee2@example.invalid";
		e2.department = "Operations";
		e2.salary = new BigDecimal("78000.00");
		e2.active = true;

		EmployeeInput e3 = new EmployeeInput();
		e3.name = "Employee 3";
		e3.email = "employee3@example.invalid";
		e3.department = "Finance";
		e3.salary = new BigDecimal("88000.00");
		e3.active = false;

		create(e1);
		create(e2);
		create(e3);
	}

	public List<Employee> list() throws SQLException {
		String sql = "SELECT id, name, email, department, salary, active, created_at, updated_at FROM employees ORDER BY updated_at DESC";
		List<Employee> out = new ArrayList<>();
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					out.add(fromRow(rs));
				}
			}
		}
		return out;
	}

	public Optional<Employee> getById(String id) throws SQLException {
		String sql = "SELECT id, name, email, department, salary, active, created_at, updated_at FROM employees WHERE id = ?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					return Optional.empty();
				}
				return Optional.of(fromRow(rs));
			}
		}
	}

	public Employee create(EmployeeInput input) throws SQLException {
		Instant now = Instant.now();
		String id = UUID.randomUUID().toString();

		String sql = "INSERT INTO employees (id, name, email, department, salary, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, id);
			stmt.setString(2, requireNonBlank(input.name, "name"));
			stmt.setString(3, requireNonBlank(input.email, "email"));
			stmt.setString(4, requireNonBlank(input.department, "department"));
			stmt.setBigDecimal(5, requireNonNull(input.salary, "salary"));
			stmt.setBoolean(6, input.active != null ? input.active : true);
			stmt.setTimestamp(7, Timestamp.from(now));
			stmt.setTimestamp(8, Timestamp.from(now));
			stmt.executeUpdate();
		}

		return getById(id).orElseThrow(() -> new SQLException("Insert failed"));
	}

	public Optional<Employee> update(String id, EmployeeInput input) throws SQLException {
		Instant now = Instant.now();

		String sql = "UPDATE employees SET name=?, email=?, department=?, salary=?, active=?, updated_at=? WHERE id=?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, requireNonBlank(input.name, "name"));
			stmt.setString(2, requireNonBlank(input.email, "email"));
			stmt.setString(3, requireNonBlank(input.department, "department"));
			stmt.setBigDecimal(4, requireNonNull(input.salary, "salary"));
			stmt.setBoolean(5, input.active != null ? input.active : true);
			stmt.setTimestamp(6, Timestamp.from(now));
			stmt.setString(7, id);

			int updated = stmt.executeUpdate();
			if (updated == 0) {
				return Optional.empty();
			}
		}

		return getById(id);
	}

	public boolean delete(String id) throws SQLException {
		String sql = "DELETE FROM employees WHERE id=?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, id);
			return stmt.executeUpdate() > 0;
		}
	}

	private static Employee fromRow(ResultSet rs) throws SQLException {
		Employee e = new Employee();
		e.id = rs.getString("id");
		e.name = rs.getString("name");
		e.email = rs.getString("email");
		e.department = rs.getString("department");
		e.salary = rs.getBigDecimal("salary");
		e.active = rs.getBoolean("active");
		Timestamp created = rs.getTimestamp("created_at");
		Timestamp updated = rs.getTimestamp("updated_at");
		e.createdAtIso = created != null ? created.toInstant().toString() : Instant.EPOCH.toString();
		e.updatedAtIso = updated != null ? updated.toInstant().toString() : Instant.EPOCH.toString();
		return e;
	}

	private static String requireNonBlank(String value, String field) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(field + " is required");
		}
		return value.trim();
	}

	private static <T> T requireNonNull(T value, String field) {
		if (value == null) {
			throw new IllegalArgumentException(field + " is required");
		}
		return value;
	}
}
