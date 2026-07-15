package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.GogroupPalletNumberMap;

@Repository
public interface GogroupPalletNumberMapRepository extends JpaRepository<GogroupPalletNumberMap, Long> {

//	  @Query(value ="SELECT g.pallet_type FROM Gogroup_palletnumber_map g WHERE g.pallet_number = :palletNumber",nativeQuery = true)
//	String findByPalletNumber( String palletNumber);

	@Query(value ="SELECT * FROM Gogroup_palletnumber_map g LEFT JOIN gogroup go ON go.goname=g.bin_id WHERE g.pallet_number = :palletNumber AND go.dept= :dept ",nativeQuery = true)
Optional<GogroupPalletNumberMap> findByPalletNumber( String palletNumber , Long dept);

boolean existsByPalletNumber(String palletNumber);

@Query(value ="SELECT * FROM Gogroup_palletnumber_map g LEFT JOIN gogroup go ON go.goname=g.bin_id WHERE  (lower(g.pallet_type) LIKE ('%' || lower('ASRS') || '%')) AND g.pallet_number = :palletNumber AND go.dept= :dept ",nativeQuery = true)
Optional<GogroupPalletNumberMap> findByAsrsPalletNumber(String palletNumber, Long dept);

@Query(value ="SELECT DISTINCT g.* FROM Gogroup_palletnumber_map g " +
        " left join asrs_pallet ap on ap.palletnumber  = g.pallet_number " +
        " left join asrs_production_detail apd on apd.id = ap.asrsproductionid " +
        " WHERE g.bin_id = :binId and g.pallet_type='Packing'and apd.productid =:productId",nativeQuery = true)
List<GogroupPalletNumberMap> findByBinId(Long binId, Long productId);



Optional<GogroupPalletNumberMap> findByPalletNumberAndPalletType(String palletNumber, String palletType);

Optional<GogroupPalletNumberMap> findByPalletNumber(String palletNumber);
@Query(value ="SELECT * FROM Gogroup_palletnumber_map g LEFT JOIN gogroup go ON go.goname=g.bin_id WHERE g.bin_id = :binNo ",nativeQuery = true)
List<GogroupPalletNumberMap> findByPalletDetails(Long binNo);

}
