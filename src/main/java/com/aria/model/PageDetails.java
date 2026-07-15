package com.aria.model;

import com.aria.dto.PageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "page_details")
public class PageDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String pageName;

    @Column
    private String pageType;

    @Column
    private String parentMenu;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;

    @Column
    private String userLevelAccess;

    @Column
    private int active;

    @Column(updatable = false)
    private Long createdBy;

    @Column
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "pageId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "active=0")
    private List<PageField> fieldList;


    public PageDetails(PageDto page) {
        this.id = page.getId();
        this.pageName = page.getPageName();
        this.pageType = page.getPageType();
        this.parentMenu = page.getParentMenu();
        this.userLevelAccess = page.getUserLevelAccess();
        this.active = page.getActive();
        this.createdBy = page.getCreatedBy();
        this.updatedBy = page.getUpdatedBy();
        this.created_date = page.getCreated_date();
        this.updated_date = page.getUpdated_date();
    }
}
