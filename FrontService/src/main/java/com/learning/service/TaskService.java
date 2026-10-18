package com.learning.service;

import com.learning.client.TaskServiceClient;
import com.learning.dto.Task;
import com.learning.repository.InMemoryTaskRepository;
import com.learning.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository repository = new InMemoryTaskRepository();

    @Autowired
    private TaskServiceClient taskServiceClient;

    public List<Task> getTasksByDate(LocalDate date, String userId, String jwtToken) {
        // Convert LocalDate to String (yyyy-MM-dd)
        String dateString = date.toString();
        // Get the body from ResponseEntity<List<Task>>
        return taskServiceClient.getTask(userId, dateString, "Bearer " + jwtToken).getBody();
    }

    public List<Task> getAllTasks() {
        if (repository instanceof InMemoryTaskRepository) {
            return ((InMemoryTaskRepository) repository).findAll();
        }
        throw new UnsupportedOperationException("getAllTasks is only supported on InMemoryTaskRepository");
    }

    public Task addTask(Task task, String jwtToken) {
        //return repository.save(task);
        return taskServiceClient.createTask(task, "Bearer " + jwtToken).getBody();
    }

    public Task updateTask(Task task, String jwtToken) {
        return taskServiceClient.updateTask(task, "Bearer " + jwtToken).getBody();
        //return repository.update(task);
    }

    public void deleteTask(Long id) {
        repository.delete(id);
    }

    public Task getTask(Long id) {
        return repository.findById(id);
    }
}
