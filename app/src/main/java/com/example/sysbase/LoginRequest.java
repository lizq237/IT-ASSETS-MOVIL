package com.example.sysbase;

public class LoginRequest {
    private String email;
    private String password;
    private String device_name;

    public LoginRequest(String email, String password, String device_name) {
        this.email = email;
        this.password = password;
        this.device_name = device_name;
    }

    // Getters obligatorios para que Gson pueda serializar el objeto
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getDevice_name() {
        return device_name;
    }
}
