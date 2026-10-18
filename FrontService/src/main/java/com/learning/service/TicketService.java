package com.learning.service;

import com.learning.dto.Ticket;
import com.learning.repository.InMemoryTicketRepository;

import java.util.List;

public class TicketService {
    private final InMemoryTicketRepository ticketRepo = new InMemoryTicketRepository();

    public List<Ticket> getAllTickets() {
        return ticketRepo.findAll();
    }

    public Ticket addTicket(String summary, String description, String status) {
        Ticket ticket = new Ticket(null, summary, description, status);
        return ticketRepo.save(ticket);
    }

    public Ticket updateTicket(Long id, String summary, String description, String status) {
        Ticket ticket = ticketRepo.findById(id);
        if (ticket != null) {
            ticket.setSummary(summary);
            ticket.setDescription(description);
            ticket.setStatus(status);
            ticketRepo.update(ticket);
        }
        return ticket;
    }

    public Ticket getTicketById(Long id) {
        return ticketRepo.findById(id);
    }
}
