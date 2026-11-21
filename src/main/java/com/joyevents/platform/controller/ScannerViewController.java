package com.joyevents.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ScannerViewController {

    /**
     * Maps clean URL: /scan/{conferenceCode} (e.g. /scan/PHO26, /scan/PHOSS)
     * Forwards directly to the mobile scanner page with zero page reload.
     */
    @GetMapping("/scan/{conferenceCode}")
    public String openEventScanner(@PathVariable String conferenceCode) {
        return "forward:/scanner.html";
    }
}