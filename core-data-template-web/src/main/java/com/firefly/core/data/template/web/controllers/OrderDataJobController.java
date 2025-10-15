package com.firefly.core.data.template.web.controllers;

import com.firefly.common.data.controller.AbstractDataJobController;
import com.firefly.common.data.service.DataJobService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/jobs")
public class OrderDataJobController extends AbstractDataJobController {

    public OrderDataJobController(
            @Qualifier("orderDataJobService") DataJobService dataJobService) {
        super(dataJobService);
    }
}
