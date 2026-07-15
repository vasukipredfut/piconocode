package com.aria.model;

import com.aria.dto.MachineClusterItemsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine_cluster_items")
public class MachineClusterItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
	@JoinColumn(name = "machine_id", referencedColumnName = "id")
	private Machine machine;
    
    @Column(name = "cluster_id")
	private Long clusterId;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public MachineClusterItems(MachineClusterItemsDto field) {
    	 this.id = field.getId();
    	 if (field.getAuxMachineId() != null) {
  			Machine mc = new Machine();
  			mc.setId(field.getAuxMachineId());
  			this.machine = mc;
  		}
    	 this.clusterId = field.getClusterId();
         this.createdBy = field.getCreatedBy();
 		this.created_date = field.getCreated_date();
 		this.updatedBy = field.getUpdatedBy();
 		this.updated_date = field.getUpdated_date();
    }
}
