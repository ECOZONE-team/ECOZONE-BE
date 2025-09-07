package co.ecozone.ecozoneapi.inquiry.infrastructure.web.controller;

import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquiryDetail;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquirySummary;
import co.ecozone.ecozoneapi.inquiry.application.service.InquiryService;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.*;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper.InquiryApiMapper;
import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inquiries")
@AllArgsConstructor
public class InquiryController {

    private final InquiryService service;
    private final InquiryApiMapper mapper;

    @PostMapping
    public ResponseEntity<InquiryDetailResponse> create(
            @RequestBody InquiryCreateRequest req,
            @AuthenticationPrincipal JwtPrincipal principal) {

        CreateInquiryCommand cmd = mapper.toCommand(req, principal.userId().value());
        InquiryDetail detail = service.createInquiry(cmd);

        InquiryDetailResponse response = mapper.toResponse(detail);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(detail.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InquiryListResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal) {

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        List<InquirySummary> summaries = service.listInquiries(principal.userId().value(), isAdmin);

        List<InquiryListResponse> responseList = summaries.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        InquiryDetail detail = service.getInquiry(id, principal.userId().value(), isAdmin);

        InquiryDetailResponse response = mapper.toResponse(detail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/answer")
    public ResponseEntity<InquiryDetailResponse> answer(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        boolean isAdmin = principal.roles().contains(Role.ADMIN);
        InquiryDetail detail = service.markAsAnswered(id, principal.userId().value(), isAdmin);

        InquiryDetailResponse response = mapper.toResponse(detail);
        return ResponseEntity.ok(response);
    }
}
