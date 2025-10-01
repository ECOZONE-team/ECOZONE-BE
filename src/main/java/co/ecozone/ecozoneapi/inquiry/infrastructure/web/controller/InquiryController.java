package co.ecozone.ecozoneapi.inquiry.infrastructure.web.controller;

import co.ecozone.ecozoneapi.auth.infrastructure.security.JwtPrincipal;
import co.ecozone.ecozoneapi.auth.domain.model.security.Role;
import co.ecozone.ecozoneapi.inquiry.application.command.CreateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.command.UpdateInquiryCommand;
import co.ecozone.ecozoneapi.inquiry.application.service.InquiryService;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.dto.*;
import co.ecozone.ecozoneapi.inquiry.infrastructure.web.mapper.InquiryApiMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@AllArgsConstructor
public class InquiryController {

    private final InquiryService service;
    private final InquiryApiMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InquiryDetailResponse> create(
            @RequestBody @Valid InquiryCreateRequest req,
            @AuthenticationPrincipal JwtPrincipal principal) {

        CreateInquiryCommand cmd = mapper.toCommand(req, principal.userId().value());
        InquiryDetailResponse response = service.createInquiry(cmd);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InquiryListResponse>> list(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        boolean isAdmin = principal.roles().stream().anyMatch(role -> role == Role.ADMIN);
        Pageable pageable = PageRequest.of(page, size);
        List<InquiryListResponse> summaries = service.listInquiries(principal.userId().value(), isAdmin, pageable)
                .getContent();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InquiryDetailResponse> get(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        boolean isAdmin = principal.roles().stream().anyMatch(role -> role == Role.ADMIN);
        InquiryDetailResponse response = service.getInquiry(id, principal.userId().value(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryDetailResponse> answer(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InquiryDetailResponse response = service.markAsAnswered(id, principal.userId().value());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<InquiryDetailResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid InquiryUpdateRequest req,
            @AuthenticationPrincipal JwtPrincipal principal) {

        UpdateInquiryCommand cmd = new UpdateInquiryCommand(
                id, null, req.name(), req.phone(), req.note(), principal.userId().value());
        InquiryDetailResponse response = service.updateInquiry(cmd, principal.userId().value());
        return ResponseEntity.ok(response);
    }
}
