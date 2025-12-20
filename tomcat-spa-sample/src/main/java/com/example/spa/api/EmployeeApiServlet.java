package com.example.spa.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EmployeeApiServlet extends HttpServlet {
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private EmployeeRepository repo;

	@Override
	public void init() throws ServletException {
		try {
			repo = new EmployeeRepository(Db.getDataSource());
			repo.initSchema();
			repo.seedIfEmpty();
		} catch (Exception e) {
			throw new ServletException("Failed to initialize DB", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		String pathInfo = req.getPathInfo(); // e.g. /employees or /employees/{id}
		try {
			if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/employees")) {
				List<Employee> employees = repo.list();
				writeJson(resp, HttpServletResponse.SC_OK, employees);
				return;
			}

			String[] parts = trimSlashes(pathInfo).split("/");
			if (parts.length == 2 && parts[0].equals("employees")) {
				Optional<Employee> employee = repo.getById(parts[1]);
				if (employee.isEmpty()) {
					writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
					return;
				}
				writeJson(resp, HttpServletResponse.SC_OK, employee.get());
				return;
			}

			writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
		} catch (Exception e) {
			writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error(e.getMessage()));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.equals("/") || !trimSlashes(pathInfo).equals("employees")) {
			writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
			return;
		}

		try {
			EmployeeInput input = MAPPER.readValue(req.getInputStream(), EmployeeInput.class);
			Employee created = repo.create(input);
			writeJson(resp, HttpServletResponse.SC_CREATED, created);
		} catch (IllegalArgumentException e) {
			writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, error(e.getMessage()));
		} catch (Exception e) {
			writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error(e.getMessage()));
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		String pathInfo = req.getPathInfo();
		String[] parts = pathInfo == null ? new String[0] : trimSlashes(pathInfo).split("/");
		if (parts.length != 2 || !parts[0].equals("employees")) {
			writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
			return;
		}

		try {
			EmployeeInput input = MAPPER.readValue(req.getInputStream(), EmployeeInput.class);
			Optional<Employee> updated = repo.update(parts[1], input);
			if (updated.isEmpty()) {
				writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
				return;
			}
			writeJson(resp, HttpServletResponse.SC_OK, updated.get());
		} catch (IllegalArgumentException e) {
			writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, error(e.getMessage()));
		} catch (Exception e) {
			writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error(e.getMessage()));
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		String pathInfo = req.getPathInfo();
		String[] parts = pathInfo == null ? new String[0] : trimSlashes(pathInfo).split("/");
		if (parts.length != 2 || !parts[0].equals("employees")) {
			writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
			return;
		}

		try {
			boolean deleted = repo.delete(parts[1]);
			if (!deleted) {
				writeJson(resp, HttpServletResponse.SC_NOT_FOUND, error("Not found"));
				return;
			}
			writeJson(resp, HttpServletResponse.SC_OK, ok());
		} catch (Exception e) {
			writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error(e.getMessage()));
		}
	}

	private static void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
		resp.setStatus(status);
		MAPPER.writeValue(resp.getOutputStream(), body);
	}

	private static Object error(String message) {
		return new Object() {
			public final String error = message == null ? "Request failed" : message;
		};
	}

	private static Object ok() {
		return new Object() {
			public final boolean ok = true;
		};
	}

	private static String trimSlashes(String path) {
		String p = path;
		while (p.startsWith("/")) p = p.substring(1);
		while (p.endsWith("/")) p = p.substring(0, p.length() - 1);
		return p;
	}
}
