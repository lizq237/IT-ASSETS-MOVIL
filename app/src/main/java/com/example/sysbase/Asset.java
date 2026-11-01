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

    // Evita que aparezca "null" en pantalla si no viene modelo
    public String getModel() {
        return model != null ? model : "No especificado";
    }

    // Evita que aparezca "null" en pantalla si no viene número de serie[cite: 4]
    public String getSerial_number() {
        return serial_number != null ? serial_number : "Sin serie";
    }

    public String getStatus() { return status; }
    public String getCreated_at() { return created_at; }
}