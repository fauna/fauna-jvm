package com.fauna.beans;


public class Person {

    private String firstName;

    private String lastName;

    private char middleInitial;

    private int age;

    public Person(String firstName, String lastName, char middleInitial, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleInitial = middleInitial;
        this.age = age;
    }

    public Person() {

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public char getMiddleInitial() {
        return middleInitial;
    }

    public int getAge() {
        return age;
    }
}
