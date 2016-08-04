package com.paymentprocessor.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class StripeOauthClientController {
	@Value("${example.message}")
    private String message;

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    @ResponseBody
    public String getMessage() {
        return message;
    }

    @RequestMapping(value = "/students-ftl", method = RequestMethod.GET)
    public String loadFtl() {
        return "students-ftl";
    }
    
    @RequestMapping(value = "/students-jsp", method = RequestMethod.GET)
    public String loadStudentsJsp() {
        return "students-jsp";
    }
}
