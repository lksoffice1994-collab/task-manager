package com.learning.controller;

import com.learning.dto.Task;
import com.learning.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String showTasks(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "userId", required = false) String userId,
            @RequestParam(value = "jwt", required = false) String jwtToken,
            Model model
    ) {
        LocalDate selectedDate = date != null ? date : LocalDate.now();
        // Fill in calendar days for the month view
        List<Map<String, Object>> calendarDays = buildCalendar(selectedDate);
        List<Task> tasks = taskService.getTasksByDate(selectedDate, userId, jwtToken);

        model.addAttribute("selectedMonthYear",
                selectedDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + selectedDate.getYear());
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("selectedDate", selectedDate.toString());
        model.addAttribute("selectedDateReadable", selectedDate.toString());
        model.addAttribute("selectedMonth", selectedDate.getMonthValue());
        model.addAttribute("selectedYear", selectedDate.getYear());
        model.addAttribute("tasks", tasks);
        model.addAttribute("isTodaySelected", selectedDate.equals(LocalDate.now()));
        model.addAttribute("userId", userId);
        model.addAttribute("jwtToken", jwtToken);

        return "tasks";
    }

    @PostMapping
    public String addTask(@ModelAttribute Task task,
                          @RequestParam("jwt") String jwtToken,
                          @RequestParam("userId") String userId) {
        // You can use jwtToken and userId here for audit/security
        System.out.println("Add Task - userId: " + userId + ", jwt: " + jwtToken);
        task.setUserId(userId);
        taskService.addTask(task, jwtToken);
        return "redirect:/tasks?userId=" + userId + "&jwt=" + jwtToken + "&date=" + task.getDate();
    }

    @PostMapping("/{id}")
    public String updateTask(@PathVariable("id") Long id,
                             @ModelAttribute Task task,
                             @RequestParam("jwt") String jwtToken,
                             @RequestParam("userId") String userId) {
        // You can use jwtToken and userId here for audit/security
        System.out.println("Update Task - userId: " + userId + ", jwt: " + jwtToken);
        task.setId(id);
        taskService.updateTask(task, jwtToken);
        return "redirect:/tasks?userId=" + userId + "&jwt=" + jwtToken + "&date=" + task.getDate();
    }

    // Helper for building the calendar grid for the template
    private List<Map<String, Object>> buildCalendar(LocalDate selectedDate) {
        List<Map<String, Object>> days = new ArrayList<>();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday=0
        int lengthOfMonth = selectedDate.lengthOfMonth();

        // Add blanks before the 1st
        for (int i = 0; i < firstDayOfWeek; ++i) {
            days.add(blankDay());
        }
        // Fill actual days
        for (int day = 1; day <= lengthOfMonth; ++day) {
            LocalDate cur = firstOfMonth.withDayOfMonth(day);
            Map<String, Object> dayMeta = new HashMap<>();
            dayMeta.put("dayOfMonth", day);
            dayMeta.put("date", cur.toString());
            dayMeta.put("selected", cur.equals(selectedDate));
            days.add(dayMeta);
        }
        // Add trailing blanks so days.size() is a multiple of 7 (for full weeks)
        int remainder = days.size() % 7;
        if (remainder != 0) {
            int blanksToAdd = 7 - remainder;
            for (int i = 0; i < blanksToAdd; ++i) {
                days.add(blankDay());
            }
        }
        // If less than 6 weeks (42 cells) fill additional blanks for 6 total rows
        // while (days.size() < 42) {
        //     days.add(blankDay());
        // }
        return days;
    }

    private Map<String, Object> blankDay() {
        Map<String, Object> blank = new HashMap<>();
        blank.put("dayOfMonth", "");
        blank.put("date", "");
        blank.put("selected", false);
        return blank;
    }
}
