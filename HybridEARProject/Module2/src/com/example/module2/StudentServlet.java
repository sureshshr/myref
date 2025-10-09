package com.example.module2;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class StudentServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = getServletConfig().getInitParameter("db.url");
        String user = getServletConfig().getInitParameter("db.user");
        String pwd = getServletConfig().getInitParameter("db.password");
        if (url == null) url = "jdbc:sqlserver://localhost:1433;databaseName=StudentDB";
        if (user == null) user = "sa";
        if (pwd == null) pwd = "yourStrong(!)Password";

        try {
            StudentDAO dao = new StudentDAO(url, user, pwd);
            List<Student> list = dao.listStudents();
            req.setAttribute("students", list);
            req.getRequestDispatcher("/students.jsp").forward(req, resp);
            return;
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Error: " + ex.getMessage());
            ex.printStackTrace(resp.getWriter());
        }
    }
}
