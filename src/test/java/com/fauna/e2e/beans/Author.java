package com.fauna.e2e.beans;

public class Author {

    private String firstName;

    private String lastName;

    private String middleInitial;

    private int age;

    public Author(String firstName, String lastName, String middleInitial,
                  int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleInitial = middleInitial;
        this.age = age;
    }

    public Author() {

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public int getAge() {
        return age;
    }
}
