package com.learning.repository;

import com.learning.dto.Task;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryTaskRepository implements TaskRepository {
    private final Map<Long, Task> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Task> findByDate(LocalDate date) {
        return storage.values().stream()
                .filter(task -> date.equals(task.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    public Task save(Task task) {
        long id = idGen.getAndIncrement();
        task.setId(id);
        storage.put(id, task);
        return task;
    }

    @Override
    public Task update(Task task) {
        if (task.getId() == null || !storage.containsKey(task.getId())) {
            throw new IllegalArgumentException("Task not found for update");
        }
        storage.put(task.getId(), task);
        return task;
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }

    @Override
    public Task findById(Long id) {
        return storage.get(id);
    }
}
