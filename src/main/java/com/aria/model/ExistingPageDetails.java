package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.ExistingPageDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "existing_page_details")
public class ExistingPageDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_name")
    private String pageName;

    @Column(name = "page_type")
    private String pageType;

    @OneToOne
	@JoinColumn(name = "page_created_by", referencedColumnName = "id")
	private User pageCreated;
    
//    @Column
//    private Long pageCreatedBy;

    @Column(name = "page_created_date")
    private Date pageCreatedDate;

    
//    @Column
//    private int active;

    @Column(name = "created_By", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "pageId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @Where(clause = "active=0")
    private List<FieldsInExistingPage> fieldList;

	public ExistingPageDetails(ExistingPageDto page) {
		this.id = page.getId();
		this.pageName = page.getPageName();
		this.pageType = page.getPageType();
		if (page.getPageCreatedBy() != null) {
			User user = new User();
			user.setId(page.getPageCreatedBy());
			this.pageCreated = user;
		}
//		this.pageCreatedBy = page.getPageCreatedBy();
//		this.pageCreatedDate = page.getPageCreatedDate();
//		this.active = page.getA;
		this.createdBy = page.getCreatedBy();
		this.updatedBy = page.getUpdatedBy();
		this.created_date = page.getCreated_date();
		this.updated_date = page.getUpdated_date();
	}


    

}
