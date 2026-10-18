package com.learning.repository;

import com.learning.dto.Ticket;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTicketRepository {
    private final Map<Long, Ticket> tickets = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(idGenerator.getAndIncrement());
        }
        tickets.put(ticket.getId(), ticket);
        return ticket;
    }

    public Ticket findById(Long id) {
        return tickets.get(id);
    }

    public void update(Ticket ticket) {
        if (ticket.getId() != null && tickets.containsKey(ticket.getId())) {
            tickets.put(ticket.getId(), ticket);
        }
    }
}
