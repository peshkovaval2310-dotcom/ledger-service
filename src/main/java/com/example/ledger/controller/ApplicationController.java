package com.example.ledger.controller;

import com.example.ledger.service.ApplicationService;

import java.util.UUID;

public class ApplicationController {

    private final ApplicationService applicationService;


    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public void blockLenderLimit(UUID id){
        applicationService.blockLenderLimit(id);
    }
}
