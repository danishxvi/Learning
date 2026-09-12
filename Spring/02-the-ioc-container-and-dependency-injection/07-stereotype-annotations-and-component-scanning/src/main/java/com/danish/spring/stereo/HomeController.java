package com.danish.spring.stereo;

import org.springframework.stereotype.Controller;

// @Controller marks a class as a web layer component. Without spring-boot-starter-web on
// the classpath (as here), it registers as a plain bean and does nothing special - the
// web-specific behaviour (mapping HTTP requests to methods via @GetMapping etc.) is added
// by DispatcherServlet auto-configuration, which only activates once Spring MVC is on the
// classpath. Section 04 uses @RestController (itself @Controller + @ResponseBody merged)
// for every REST endpoint - this class exists here only to complete the stereotype list.
@Controller
public class HomeController {

    public String greeting() {
        return "Hello from a plain @Controller bean - no web server involved yet.";
    }
}
