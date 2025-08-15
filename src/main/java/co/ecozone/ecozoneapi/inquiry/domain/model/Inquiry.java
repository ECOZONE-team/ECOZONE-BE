package co.ecozone.ecozoneapi.inquiry.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private Long companyIdx;

    private LocalDateTime regDate = LocalDateTime.now();

    private String companyName;

    private String name;

    private String phone;

    @Column(length = 2000)
    private String note;

    private String createdBy; // JWT username 저장

    // Getter/Setter
    public Long getIdx() { return idx; }
    public void setIdx(Long idx) { this.idx = idx; }

    public Long getCompanyIdx() { return companyIdx; }
    public void setCompanyIdx(Long companyIdx) { this.companyIdx = companyIdx; }

    public LocalDateTime getRegDate() { return regDate; }
    public void setRegDate(LocalDateTime regDate) { this.regDate = regDate; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
