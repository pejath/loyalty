package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.MemberDtos.MemberDto;
import com.clubloyalty.server.service.member.MemberService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService ms;

    public MemberController(MemberService m) {
        this.ms = m;
    }

    @GetMapping("/me")
    public MemberDto me(Authentication a) {
        return ms.me(a.getName());
    }

    @GetMapping("/{id}")
    public MemberDto get(@PathVariable Long id) {
        return ms.get(id);
    }
}
