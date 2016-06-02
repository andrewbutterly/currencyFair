package org.cf.trade;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catch all error handler
 * 
 * returns HTTP status code and nothing else
 * */
@RestController
public class CustomErrorController implements ErrorController {

    private static final String PATH = "/error";

    private static final ResponseEntity<String> BAD_REQUEST = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    
    @RequestMapping(value = PATH)
    ResponseEntity<String> error(HttpServletRequest request, HttpServletResponse response) {
    	return BAD_REQUEST;
    }
    
    @Override
    public String getErrorPath() {
        return PATH;
    }
    
    
}
