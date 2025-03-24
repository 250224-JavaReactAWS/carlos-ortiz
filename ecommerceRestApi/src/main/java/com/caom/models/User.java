package com.caom.models;

public class User implements Comparable<User>{


    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
    private static int userIdCounter = 1;

    public User(String firstName, String lastName, String email, String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = Role.USER;
        this.userId = userIdCounter;
        userIdCounter++;
    }

    public User(int userId, String firstName, String lastName, String email, String password){
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }

    public User(){

    }

    public int getUserId(){
        return userId;
    }

    public void setUserId(int userId){
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public int compareTo(User o) {

        // What is the COMPARABLE Interface? Why is it important? What do we use it for?
        // Comparable is an Interface used to define the NATURAL ordering of a class (the default way you want
        // objects of this class sorted).

        // For users I want them sorted by their UserIds

        // compareTo is a method that takes in an object of the same type and allows you to perform some operation for
        // sorting

        // Positive numbers means this comes after
        // Negative numbers means this comes before
        // 0 means they have the same ranking
        if (this.getUserId() > o.getUserId()){
            return 1;
        } else if( this.getUserId() < o.getUserId()){
            return -1;
        } else{
            return 0;
        }
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }
}
