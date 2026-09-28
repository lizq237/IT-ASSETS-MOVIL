package com.example.sysbase;

public class LoginResponse {
    private String access_token;
    private String token_type;
    private User user;

    public String getAccessToken() { return access_token; }
    public User getUser() { return user; }

    // Subclase para mapear el objeto "user" que viene dentro del JSON
    public static class User {
        private String id;
        private String name;
        private String email;
        private String role;
        private String organization_id;

        public String getName() { return name; }
        public String getRole() { return role; }
    }
}