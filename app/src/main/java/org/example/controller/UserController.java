package org.example.controller;

import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/user")
    public List<User> getUserList() {
        return userMapper.selectList(null);
    }
}
