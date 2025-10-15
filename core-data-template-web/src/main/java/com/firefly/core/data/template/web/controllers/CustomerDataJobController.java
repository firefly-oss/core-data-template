package com.firefly.core.data.template.web.controllers;

import com.firefly.common.data.controller.AbstractDataJobController;
import com.firefly.common.data.service.DataJobService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers/jobs")
public class CustomerDataJobController extends AbstractDataJobController {

    public CustomerDataJobController(
            @Qualifier("customerDataJobService") DataJobService dataJobService) {
        super(dataJobService);
    }
}