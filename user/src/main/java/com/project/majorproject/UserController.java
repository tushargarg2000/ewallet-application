package com.project.majorproject;

import com.project.majorproject.UserRequest;
import com.project.majorproject.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController
{

    @Autowired
    UserService userService;

    @PostMapping("/add")
    String createUser(@RequestBody()UserRequest userRequest){
        return userService.addUser(userRequest);
    }

    @GetMapping("/findByUser/{userName}")
    User getUserByUserName(@PathVariable("userName")String userName){

        return userService.findUserByUserName(userName);
    }
}
