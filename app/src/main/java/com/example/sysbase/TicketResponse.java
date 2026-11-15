package com.example.sysbase;

public class TicketResponse {
    private TicketData ticket;

    public String getTicketId() {
        return ticket != null ? ticket.id : null;
    }

    public static class TicketData {
        private String id;
    }
}
