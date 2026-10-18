package com.learning.repository;

import com.learning.dto.Task;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository {
    List<Task> findByDate(LocalDate date);
    Task save(Task task);
    Task update(Task task);
    void delete(Long id);
    Task findById(Long id);
}
