package com.pwc.bitcask.test;

import java.io.Serializable;

public class Student implements Serializable {
    private final String id;
    private final String name;
    private final String age;
    private final String hobbit;
    private static final long serialVersionUID = 14334;

    public Student(String id, String name, String age, String hobbit) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.hobbit = hobbit;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || !(other instanceof Student)) {
            return false;
        }

        Student student = (Student) other;
        return student.id.equals(this.id) && student.name.equals(this.name);
    }

}
