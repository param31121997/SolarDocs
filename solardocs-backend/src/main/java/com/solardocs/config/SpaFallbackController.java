package com.solardocs.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {
    // Matches a single path segment with no "." (top-level SPA routes like /customers, /dashboard)
    @GetMapping("/{path:[^\\.]*}")
    public String forwardTopLevel() {
        return "forward:/index.html";
    }

    // Matches multi-segment paths with no "." anywhere (nested SPA routes like /customers/A00001)
    @GetMapping("/{*path}")
    public String forwardNested() {
        return "forward:/index.html";
    }
}