package com.example.module1;

public class Employee {
    private int id;
    private String name;
    private String title;

    public Employee(int id, String name, String title) {
        this.id = id;
        this.name = name;
        this.title = title;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getTitle() { return title; }
}
