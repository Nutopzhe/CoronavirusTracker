package com.nikolayrybakov.covidtracker.controller;

import com.nikolayrybakov.covidtracker.service.VirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    VirusDataService virusDataService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("statistics", virusDataService.getAllStats());
        model.addAttribute("totalReportedCases", virusDataService.getTotalCases());
        model.addAttribute("totalNewCases", virusDataService.getTotalNewCases());
        return "home";
    }
}
