package com.example.demo.controller;

import com.example.demo.DTO.OV.Result;
import com.example.demo.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    final FollowService followService;

    @GetMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") String followUserId, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(followUserId, isFollow);
    }


    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") String followUserId) {
        return followService.isFollow(followUserId);
    }

    @GetMapping("/common/{id}")
    public Result followCommon(@PathVariable("id") String id) {
        return followService.followCommons(id);
    }
}
