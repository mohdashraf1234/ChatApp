package com.chatapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    
    @GetMapping("/")
    public String home() {
        return "redirect:/chat";
    }
    
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }
}