package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "atlog")
public class PreventiveMaintainceAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name="logid")
	private Long logId;

    @Column(name="logtype")
    private String logType;

    @Column(name="schid")
    private Long schId;

    // unique refid is added for  find particular blending advice cas group completed status update
    @Column(name="mcid")
    private Long mcId;

    @Column(name="batchno")
    private String batchNo;

    @Column(name="tcid")
    private Long tcId;

    @Column(name="tgid")
    private Long tgId;

    @Column(name="taskid")
    private Long taskId;

    @Column(name="atid")
    private Long atId;

    @Column(name="resid")
    private Long resId;

    @Column(name="resval")
    private String resVal;

}
