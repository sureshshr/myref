package com.example.spa.jaxrs;

import com.example.spa.api.Db;

import javax.sql.DataSource;

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

public class StudentRepository {
	private final DataSource dataSource;

	public StudentRepository() {
		this(Db.getDataSource());
	}

	public StudentRepository(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void initSchema() throws SQLException {
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(
				"CREATE TABLE IF NOT EXISTS students (" +
						"id VARCHAR(36) PRIMARY KEY," +
						"name VARCHAR(200) NOT NULL," +
						"email VARCHAR(320) NOT NULL," +
						"major VARCHAR(100) NOT NULL," +
						"year_level INT NOT NULL," +
						"active BOOLEAN NOT NULL," +
						"created_at TIMESTAMP NOT NULL," +
						"updated_at TIMESTAMP NOT NULL" +
						")")) {
			stmt.execute();
		}
	}

	public void seedIfEmpty() throws SQLException {
		String countSql = "SELECT COUNT(*) AS c FROM students";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(countSql)) {
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next() && rs.getLong("c") > 0) {
					return;
				}
			}
		}

		create(newInput("Student 1", "student1@example.invalid", "Computer Science", 2, true));
		create(newInput("Student 2", "student2@example.invalid", "Business", 1, true));
		create(newInput("Student 3", "student3@example.invalid", "Mathematics", 3, false));
	}

	private static StudentInput newInput(String name, String email, String major, int year, boolean active) {
		StudentInput s = new StudentInput();
		s.name = name;
		s.email = email;
		s.major = major;
		s.year = year;
		s.active = active;
		return s;
	}

	public List<Student> list() throws SQLException {
		String sql = "SELECT id, name, email, major, year_level, active, created_at, updated_at FROM students ORDER BY updated_at DESC";
		List<Student> out = new ArrayList<>();
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					out.add(fromRow(rs));
				}
			}
		}
		return out;
	}

	public Optional<Student> getById(String id) throws SQLException {
		String sql = "SELECT id, name, email, major, year_level, active, created_at, updated_at FROM students WHERE id = ?";
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

	public Student create(StudentInput input) throws SQLException {
		Instant now = Instant.now();
		String id = UUID.randomUUID().toString();

		String sql = "INSERT INTO students (id, name, email, major, year_level, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, id);
			stmt.setString(2, requireNonBlank(input.name, "name"));
			stmt.setString(3, requireNonBlank(input.email, "email"));
			stmt.setString(4, requireNonBlank(input.major, "major"));
			stmt.setInt(5, requirePositiveInt(input.year, "year"));
			stmt.setBoolean(6, input.active != null ? input.active : true);
			stmt.setTimestamp(7, Timestamp.from(now));
			stmt.setTimestamp(8, Timestamp.from(now));
			stmt.executeUpdate();
		}

		return getById(id).orElseThrow(() -> new SQLException("Insert failed"));
	}

	public Optional<Student> update(String id, StudentInput input) throws SQLException {
		Instant now = Instant.now();

		String sql = "UPDATE students SET name=?, email=?, major=?, year_level=?, active=?, updated_at=? WHERE id=?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, requireNonBlank(input.name, "name"));
			stmt.setString(2, requireNonBlank(input.email, "email"));
			stmt.setString(3, requireNonBlank(input.major, "major"));
			stmt.setInt(4, requirePositiveInt(input.year, "year"));
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
		String sql = "DELETE FROM students WHERE id=?";
		try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, id);
			return stmt.executeUpdate() > 0;
		}
	}

	private static Student fromRow(ResultSet rs) throws SQLException {
		Student s = new Student();
		s.id = rs.getString("id");
		s.name = rs.getString("name");
		s.email = rs.getString("email");
		s.major = rs.getString("major");
		s.year = rs.getInt("year_level");
		s.active = rs.getBoolean("active");
		Timestamp created = rs.getTimestamp("created_at");
		Timestamp updated = rs.getTimestamp("updated_at");
		s.createdAtIso = created != null ? created.toInstant().toString() : Instant.EPOCH.toString();
		s.updatedAtIso = updated != null ? updated.toInstant().toString() : Instant.EPOCH.toString();
		return s;
	}

	private static String requireNonBlank(String value, String field) {
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalArgumentException(field + " is required");
		}
		return value.trim();
	}

	private static int requirePositiveInt(Integer value, String field) {
		if (value == null) {
			throw new IllegalArgumentException(field + " is required");
		}
		if (value <= 0) {
			throw new IllegalArgumentException(field + " must be > 0");
		}
		return value;
	}
}
