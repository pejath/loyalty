package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.MemberDtos.MemberDto;
import com.clubloyalty.server.dto.MemberDtos.MemberUpdateRequest;
import com.clubloyalty.server.dto.MemberDtos.PointsAdjustRequest;
import com.clubloyalty.server.dto.RewardDtos.TxDto;
import com.clubloyalty.server.service.member.MemberService;
import com.clubloyalty.server.service.points.PointsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/members")
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminMemberController {
    private final MemberService members;
    private final PointsService points;

    public AdminMemberController(MemberService members, PointsService points) {
        this.members = members;
        this.points = points;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public List<MemberDto> list() {
        return members.listAll();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public MemberDto update(@PathVariable Long id, @RequestBody @Validated MemberUpdateRequest request) {
        return members.update(id, request);
    }

    @PostMapping("/{id}/points")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public TxDto adjust(@PathVariable Long id, @RequestBody @Validated PointsAdjustRequest request) {
        if (request.delta == null || request.delta == 0) {
            throw new RuntimeException("Delta must be non-zero");
        }
        return points.adjust(id, request.delta);
    }
}
