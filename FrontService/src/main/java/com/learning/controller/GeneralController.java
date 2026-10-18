package com.learning.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class GeneralController {

    @GetMapping("/contact")
    public String showContactPage(Model model) {
        // No dynamic data needed, HTML is hardcoded
        return "contact";
    }

    @GetMapping("/api/contact")
    @ResponseBody
    public Map<String, String> getContactDetails() {
        Map<String, String> contact = new HashMap<>();
        contact.put("email", "contact@learning.com");
        contact.put("phone", "+91-99999-99999");
        contact.put("address", "1234, Learning Street, Tech Valley, India");
        contact.put("hours", "Mon-Fri, 9am to 6pm IST");
        return contact;
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/accessibility")
    public String accessibility() {
        return "accessibility";
    }
}
