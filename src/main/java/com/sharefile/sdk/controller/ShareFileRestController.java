package com.sharefile.sdk.controller;


import com.sharefile.sdk.controller.model.User;
import com.sharefile.sdk.controller.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;

/**
 * Created by bwilkins on 4/12/16.
 */

@RestController
@RequestMapping("/atlas/sharefile")
public class ShareFileRestController {
    private static final Logger log = LoggerFactory.getLogger(ShareFileRestController.class);


    @Autowired
    public ShareFileRestController(final UserService userService) {
        this.userService = userService;
    }


    @Autowired
    private final UserService userService;


    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String createUser(@RequestBody User user) throws ShareFileException, IOException {
        log.info("Received a request to create a sharefile user {}", user.getEmailAddress());

        try {
            return userService.save(user);
        } catch (IOException e) {
            log.error(e.getMessage(),e);

        }
        return userService.save(user);
    }

    @ExceptionHandler
    @ResponseStatus(CONFLICT)
    public String handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return e.getMessage();
    }


    @ExceptionHandler
    @ResponseStatus(BAD_REQUEST)
    public String handleBadRequestException(MalFormedHeaderException e) {
        return e.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(GATEWAY_TIMEOUT)
    public String handleShareFileServerUnavailableException(ShareFileServerUnavailableException e) {
        return e.getMessage();
    }
}



