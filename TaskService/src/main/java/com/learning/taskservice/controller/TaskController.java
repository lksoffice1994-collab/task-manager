package com.learning.taskservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.taskservice.controller.reqModel.TaskResModel;
import com.learning.taskservice.dtos.TaskDto;
import com.learning.taskservice.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{userId}/{date}")
    @PreAuthorize("hasRole('ADMIN') or principal == #userId")
    public ResponseEntity<List<TaskResModel>> getTask(@PathVariable("userId") String userId,
                                                      @PathVariable("date") String date,
                                                      @RequestHeader(value = "${authorization.token.header.name:Authorization}", required = false) String jwtToken) {

        Date parsedDate = null;
        try {
            parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().build();
        }
        List<TaskDto> taskDtos = taskService.getTaskByUserIdAndDate(userId, parsedDate);

        List<TaskResModel> taskResModels = objectMapper.convertValue(taskDtos, new TypeReference<List<TaskResModel>>() {});

        return ResponseEntity.ok(taskResModels);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or principal == #taskDto.userId")
    public ResponseEntity<TaskResModel> createTask(@RequestBody TaskDto taskDto,
                                                 @RequestHeader(value = "${authorization.token.header.name:Authorization}", required = false) String jwtToken) {
        TaskDto createdDto = taskService.createTask(taskDto);
        TaskResModel resModel = objectMapper.convertValue(createdDto, TaskResModel.class);
        return ResponseEntity.ok(resModel);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or principal == #taskDto.userId")
    public ResponseEntity<TaskResModel> updateTask(@RequestBody TaskDto taskDto,
                                                 @RequestHeader(value = "${authorization.token.header.name:Authorization}", required = false) String jwtToken) {
        TaskDto updatedDto = taskService.updateTask(taskDto);
        TaskResModel resModel = objectMapper.convertValue(updatedDto,TaskResModel.class);
        return ResponseEntity.ok(resModel);
    }

}
