package com.mobile.uasr;

public class User {
    private String username;
    private String password;
    private String name;
    private String email;
    private String gender;
    private boolean reading;
    private boolean writing;
    private boolean drawing;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String password, String name, String email, String gender, boolean reading, boolean writing, boolean drawing) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.reading = reading;
        this.writing = writing;
        this.drawing = drawing;
    }

    // Getters and Setters (generate these for all fields in Android Studio for convenience)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isReading() {
        return reading;
    }

    public void setReading(boolean reading) {
        this.reading = reading;
    }

    public boolean isWriting() {
        return writing;
    }

    public void setWriting(boolean writing) {
        this.writing = writing;
    }

    public boolean isDrawing() {
        return drawing;
    }

    public void setDrawing(boolean drawing) {
        this.drawing = drawing;
    }
}
