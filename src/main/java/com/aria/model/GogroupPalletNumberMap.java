package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.PalletDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="gogroup_palletnumber_map")
public class GogroupPalletNumberMap {

	    @Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
		 
		 @Column(name = "bin_id")
		 private Long binId;
		 
		 @Column(name="pallet_type")
		 private String palletType;
		 
	     @Column(name="pallet_number")
		 private String palletNumber;
	     
	     
	     public GogroupPalletNumberMap(PalletDto palletdto)
	     {
	    	 this.palletType=palletdto.getPalletType();
	    	 this.palletNumber=palletdto.getPalletNumber();
	    	 
	     }
	     
	     
	     
	    
}
