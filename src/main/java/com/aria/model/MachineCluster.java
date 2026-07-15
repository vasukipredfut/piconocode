package com.aria.model;

import java.time.LocalDateTime;
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

import com.aria.dto.MachineClusterDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_cluster")
public class MachineCluster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
	@JoinColumn(name = "machine_id", referencedColumnName = "id")
	private Machine machine;

    @OneToMany(mappedBy = "clusterId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MachineClusterItems> machineClusterItems;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public MachineCluster(MachineClusterDto field) {
    	 this.id = field.getId();
    	 if (field.getMachineId() != null) {
 			Machine mc = new Machine();
 			mc.setId(field.getMachineId());
 			this.machine = mc;
 		}
         this.createdBy = field.getCreatedBy();
 		this.created_date = field.getCreated_date();
 		this.updatedBy = field.getUpdatedBy();
 		this.updated_date = field.getUpdated_date();
    }
}
