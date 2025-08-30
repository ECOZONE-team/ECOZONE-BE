package co.ecozone.ecozoneapi.inquiry.infrastructure.web.controller;

import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquiryDetail;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquirySummary;
import co.ecozone.ecozoneapi.inquiry.application.service.InquiryService;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.*;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper.InquiryApiMapper;
import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService service;
    private final InquiryApiMapper mapper; // MapStruct Mapper 주입

    public InquiryController(InquiryService service, InquiryApiMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public InquiryDetailResponse create(@RequestBody InquiryCreateRequest req,
                                        @org.springframework.security.core.annotation.AuthenticationPrincipal JwtPrincipal principal) {
        CreateInquiryCommand cmd = mapper.toCommand(req, principal.userId().value());
        InquiryDetail detail = service.createInquiry(cmd);
        return mapper.toResponse(detail);
    }

    @GetMapping
    public List<InquiryListResponse> list(@org.springframework.security.core.annotation.AuthenticationPrincipal JwtPrincipal principal) {
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        List<InquirySummary> summaries = service.listInquiries(principal.userId().value(), isAdmin);
        return summaries.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public InquiryDetailResponse get(@PathVariable Long id,
                                     @org.springframework.security.core.annotation.AuthenticationPrincipal JwtPrincipal principal) {
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        InquiryDetail detail = service.getInquiry(id, principal.userId().value(), isAdmin);
        return mapper.toResponse(detail);
    }

    @PostMapping("/{id}/answer")
    public InquiryDetailResponse answer(@PathVariable Long id,
                                        @org.springframework.security.core.annotation.AuthenticationPrincipal JwtPrincipal principal) {
        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        InquiryDetail detail = service.markAsAnswered(id, principal.userId().value(), isAdmin);
        return mapper.toResponse(detail);
    }
}
