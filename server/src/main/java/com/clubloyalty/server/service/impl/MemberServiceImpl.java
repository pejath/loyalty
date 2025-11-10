package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.Member;
import com.clubloyalty.server.domain.Tier;
import com.clubloyalty.server.dto.MemberDtos.MemberDto;
import com.clubloyalty.server.dto.MemberDtos.MemberUpdateRequest;
import com.clubloyalty.server.repo.MemberRepository;
import com.clubloyalty.server.repo.TierRepository;
import com.clubloyalty.server.service.member.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository members;
    private final TierRepository tiers;

    public MemberServiceImpl(MemberRepository members, TierRepository tiers) {
        this.members = members;
        this.tiers = tiers;
    }

    public MemberDto me(String username) {
        var member = members.findByUserUsername(username)
                .or(() -> members.findByPhone(username))
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return map(member);
    }

    public MemberDto get(Long id) {
        var member = members.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return map(member);
    }

    public List<MemberDto> listAll() {
        return members.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    @Transactional
    public MemberDto update(Long memberId, MemberUpdateRequest request) {
        var member = members.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.getPhone().equals(request.phone) &&
                members.existsByPhoneAndIdNot(request.phone, memberId)) {
            throw new RuntimeException("Phone already in use");
        }

        member.setFullName(request.fullName);
        member.setPhone(request.phone);

        if (!member.getTier().getId().equals(request.tierId)) {
            Tier tier = tiers.findById(request.tierId)
                    .orElseThrow(() -> new RuntimeException("Tier not found"));
            member.setTier(tier);
        }

        return map(member);
    }

    private MemberDto map(Member m) {
        var dto = new MemberDto();
        dto.id = m.getId();
        dto.fullName = m.getFullName();
        dto.phone = m.getPhone();
        dto.tier = m.getTier().getName();
        dto.tierId = m.getTier().getId();
        dto.points = m.getBalance() != null ? m.getBalance().getPoints() : 0;
        if (m.getUser() != null) {
            dto.userId = m.getUser().getId();
            dto.username = m.getUser().getUsername();
        }
        return dto;
    }
}
