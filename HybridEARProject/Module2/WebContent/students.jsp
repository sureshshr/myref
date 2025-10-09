<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="java.util.List" %>
        <%@ page import="com.example.module2.Student" %>
            <html>

            <head>
                <title>Students</title>
            </head>

            <body>
                <h1>Student List</h1>
                <% List<Student> students = (List<Student>) request.getAttribute("students"); %>
                        <ul>
                            <% if (students !=null) { for (Student s : students) { %>
                                <li>
                                    <%= s.getId() %> - <%= s.getName() %> (<%= s.getMajor() %>)
                                </li>
                                <% } } else { %>
                                    <li>No data available</li>
                                    <% } %>
                        </ul>
            </body>

            </html>