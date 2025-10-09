<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="java.util.List" %>
        <%@ page import="com.example.module1.Employee" %>
            <html>

            <head>
                <title>Employees</title>
            </head>

            <body>
                <h1>Employee List</h1>
                <% List<Employee> employees = (List<Employee>) request.getAttribute("employees"); %>
                        <ul>
                            <% if (employees !=null) { for (Employee e : employees) { %>
                                <li>
                                    <%= e.getId() %> - <%= e.getName() %> (<%= e.getTitle() %>)
                                </li>
                                <% } } else { %>
                                    <li>No data available</li>
                                    <% } %>
                        </ul>
            </body>

            </html>