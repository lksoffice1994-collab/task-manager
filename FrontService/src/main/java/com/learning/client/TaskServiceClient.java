package com.learning.client;

import com.learning.dto.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@FeignClient(name = "taskServiceClient", url = "${task.service.url}")
public interface TaskServiceClient {

    @GetMapping("/task/{userId}/{date}")
    ResponseEntity<List<Task>> getTask(
            @PathVariable("userId") String userId,
            @PathVariable("date") String date,
            @RequestHeader(value = "Authorization", required = false) String jwtToken
    );

    @PostMapping("/task")
    ResponseEntity<Task> createTask(
            @RequestBody Task task,
            @RequestHeader(value = "Authorization", required = false) String jwtToken
    );

    @PutMapping("/task")
    ResponseEntity<Task> updateTask(
            @RequestBody Task task,
            @RequestHeader(value = "Authorization", required = false) String jwtToken
    );
}
