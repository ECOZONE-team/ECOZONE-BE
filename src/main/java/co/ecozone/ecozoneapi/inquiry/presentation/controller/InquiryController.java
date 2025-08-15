package co.ecozone.ecozoneapi.inquiry.presentation.controller;

import co.ecozone.ecozoneapi.inquiry.application.service.InquiryService;
import co.ecozone.ecozoneapi.inquiry.domain.model.Inquiry;
import co.ecozone.ecozoneapi.inquiry.presentation.dto.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryService service;

    public InquiryController(InquiryService service) {
        this.service = service;
    }

    // 문의 작성
    @PostMapping
    public InquiryDetailResponse createInquiry(@RequestBody InquiryCreateRequest request,
                                               @AuthenticationPrincipal String username) {
        Inquiry inquiry = new Inquiry();
        inquiry.setCompanyIdx(request.companyIdx);
        inquiry.setCompanyName(request.companyName);
        inquiry.setName(request.name);
        inquiry.setPhone(request.phone);
        inquiry.setNote(request.note);
        inquiry.setCreatedBy(username);

        return InquiryDetailResponse.from(service.createInquiry(inquiry));
    }

    // 문의 목록 조회
    @GetMapping
    public List<InquiryListResponse> listInquiries() {
        List<Inquiry> inquiries = service.getInquiries(null, true); // null, true → 모든 사용자가 조회
        return inquiries.stream()
                .map(InquiryListResponse::from)
                .collect(Collectors.toList());

        /*
        // 관리자와 작성자만 조회 가능
        @GetMapping
        public List<InquiryListResponse> listInquiries(@AuthenticationPrincipal String username,
                                                       @AuthenticationPrincipal(expression = "roles.contains('ADMIN')") boolean isAdmin) {
            return service.getInquiries(username, isAdmin)
                          .stream()
                          .map(InquiryListResponse::from)
                          .collect(Collectors.toList());
        }
        */
    }

    // 문의 상세 조회
    @GetMapping("/{id}")
    public InquiryDetailResponse getInquiry(@PathVariable Long id) {
        Inquiry inquiry = service.getInquiry(id, null, true); // null, true → 모든 사용자가 조회
        return InquiryDetailResponse.from(inquiry);

        /*
        // 관리자와 작성자만 조회 가능
        @GetMapping("/{id}")
        public InquiryDetailResponse getInquiry(@PathVariable Long id,
                                                @AuthenticationPrincipal String username,
                                                @AuthenticationPrincipal(expression = "roles.contains('ADMIN')") boolean isAdmin) {
            return InquiryDetailResponse.from(service.getInquiry(id, username, isAdmin));
        }
        */
    }
}
