package com.example.sysbase;

public class Asset {
    private String id;
    private String code;
    private String type;
    private String model;
    private String serial_number;
    private String status;
    private String created_at;

    // Getters para obtener la información en la interfaz
    public String getId() { return id; }
    public String getCode() { return code; }
    public String getType() { return type; }
    public String getModel() { return model; }
    public String getSerial_number() { return serial_number; }
    public String getStatus() { return status; }
    public String getCreated_at() { return created_at; }
}