package com.clubloyalty.server.service.member;

import com.clubloyalty.server.dto.MemberDtos.MemberDto;
import com.clubloyalty.server.dto.MemberDtos.MemberUpdateRequest;

import java.util.List;

public interface MemberService {
    MemberDto me(String username);

    MemberDto get(Long memberId);

    List<MemberDto> listAll();

    MemberDto update(Long memberId, MemberUpdateRequest request);
}
