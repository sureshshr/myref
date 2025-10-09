package com.example.module1;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class EmployeeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        // For demo, connection info is read from servlet init params or defaults
        String url = getServletConfig().getInitParameter("db.url");
        String user = getServletConfig().getInitParameter("db.user");
        String pwd = getServletConfig().getInitParameter("db.password");
        if (url == null) url = "jdbc:sqlserver://localhost:1433;databaseName=TestDb";
        if (user == null) user = "sa";
        if (pwd == null) pwd = "yourStrong(!)Password";

        // Get employee list and forward to JSP for rendering
        try {
            EmployeeDAO dao = new EmployeeDAO(url, user, pwd);
            List<Employee> list = dao.listEmployees();
            req.setAttribute("employees", list);
            req.getRequestDispatcher("/employees.jsp").forward(req, resp);
            return;
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("<html><body><p>Error: " + ex.getMessage() + "</p></body></html>");
            ex.printStackTrace(out);
        }
    }
}
