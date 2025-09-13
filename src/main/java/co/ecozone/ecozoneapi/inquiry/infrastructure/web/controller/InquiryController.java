package co.ecozone.ecozoneapi.inquiry.infrastructure.web.controller;

import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.command.UpdateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquiryDetail;
import co.ecozone.ecozoneapi.inquiry.application.dto.InquirySummary;
import co.ecozone.ecozoneapi.inquiry.application.service.InquiryService;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryCreateRequest;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryDetailResponse;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryListResponse;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.InquiryUpdateRequest;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper.InquiryApiMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InquiryListResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        boolean isAdmin = principal.roles().contains("ADMIN");
        Pageable pageable = PageRequest.of(page, size);
        Page<InquirySummary> summaries = service.listInquiries(principal.userId().value(), isAdmin, pageable);

        List<InquiryListResponse> responseList = summaries.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InquiryDetailResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        boolean isAdmin = principal.roles().contains("ADMIN");
        InquiryDetail detail = service.getInquiry(id, principal.userId().value(), isAdmin);
        InquiryDetailResponse response = mapper.toResponse(detail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryDetailResponse> answer(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InquiryDetail detail = service.markAsAnswered(id, principal.userId().value());
        InquiryDetailResponse response = mapper.toResponse(detail);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InquiryDetailResponse> update(
            @PathVariable Long id,
            @RequestBody InquiryUpdateRequest req,
            @AuthenticationPrincipal JwtPrincipal principal) {

        UpdateInquiryCommand cmd = new UpdateInquiryCommand(
                id,
                null,
                req.name(),
                req.phone(),
                req.note(),
                principal.userId().value()
        );

        InquiryDetail detail = service.updateInquiry(cmd, principal.userId().value());
        InquiryDetailResponse response = mapper.toResponse(detail);
        return ResponseEntity.ok(response);
    }
}
