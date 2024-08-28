package com.dbaesic.inventory.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "coming-soon";
    }

    @GetMapping("/settings")
    public String settings() {
        return "coming-soon";
    }
}
