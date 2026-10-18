package com.learning.taskservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.taskservice.dtos.TaskDto;
import com.learning.taskservice.entity.TaskEntity;
import com.learning.taskservice.repo.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TaskDto createTask(TaskDto task) {
        // Convert DTO to entity
        TaskEntity entity = objectMapper.convertValue(task, TaskEntity.class);
        // Save entity (JPA will assign ID)
        TaskEntity saved = taskRepository.save(entity);
        // Convert back to DTO
        return objectMapper.convertValue(saved, TaskDto.class);
    }

    @Override
    public TaskDto updateTask(TaskDto task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task ID required for update");
        }
        // Find existing entity
        TaskEntity existing = taskRepository.findById(task.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Update properties only if present (non-null)
        if (task.getDate() != null) {
            existing.setDate(task.getDate());
        }
        if (task.getSummary() != null) {
            existing.setSummary(task.getSummary());
        }
        if (task.getDescription() != null) {
            existing.setDescription(task.getDescription());
        }
        if (task.getStatus() != null) {
            existing.setStatus(task.getStatus());
        }
        if (task.getUserId() != null) {
            existing.setUserId(task.getUserId());
        }

        // Save and convert
        TaskEntity updated = taskRepository.save(existing);
        return objectMapper.convertValue(updated, TaskDto.class);
    }

    @Override
    public List<TaskDto> getTaskByUserIdAndDate(String userId, Date date) {

        List<TaskEntity> taskEntities = taskRepository.findByUserIdAndDate(userId, date);

        if (taskEntities == null || taskEntities.isEmpty()) {
            return new ArrayList<>();
        }

        List<TaskDto> taskDtos = objectMapper.convertValue(
                taskEntities, new TypeReference<List<TaskDto>>() {});

        return taskDtos;
    }
}
