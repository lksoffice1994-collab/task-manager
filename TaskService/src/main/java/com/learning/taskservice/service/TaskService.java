package com.learning.taskservice.service;

import com.learning.taskservice.dtos.TaskDto;

import java.util.Date;
import java.util.List;

public interface TaskService {

    TaskDto createTask(TaskDto task);

    TaskDto updateTask(TaskDto task);

    List<TaskDto> getTaskByUserIdAndDate(String userId, Date date);
}
