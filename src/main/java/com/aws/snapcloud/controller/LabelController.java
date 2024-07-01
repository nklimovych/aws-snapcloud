package com.aws.snapcloud.controller;

import com.aws.snapcloud.service.LabelService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/labels")
public class LabelController {
    private final LabelService labelService;

    @GetMapping("/top")
    public List<String> getTopLabels(@RequestParam int limit) {
        return labelService.findTopLabels(limit);
    }
}
