package app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HelloController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Logger bizLogger = LoggerFactory.getLogger("biz.hello");

    @RequestMapping("/")
    public String index(HttpServletRequest req) {
        logger.info("Request to / coming from {}", req.getRemoteAddr());
        bizLogger.info("hello is invoked from {}", req.getRemoteAddr());
        return "Hello, this is a web app based on springboot\n";
    }

}
