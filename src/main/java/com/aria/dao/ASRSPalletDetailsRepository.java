package com.aria.dao;

import com.aria.dto.AsrsBinList;
import com.aria.model.ASRSPalletDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ASRSPalletDetailsRepository extends JpaRepository<ASRSPalletDetails, Long> {

    @Query(value = "SELECT * FROM asrs_pallet ad WHERE ad.asrsProductionId =:arsId order by ad.id asc ", nativeQuery = true)
    List<ASRSPalletDetails> findBy(@Param("arsId") Long arsId);

    @Query(value = "SELECT COALESCE(SUM(CAST(REPLACE(NULLIF(ap.allocatedQty, ''), ',', '.') AS DOUBLE PRECISION)), 0.0) " +
            "  FROM asrs_pallet ap WHERE ap.asrsproductionid = :arsId", nativeQuery = true)
    Double allocatedQty(@Param("arsId") Long arsId);


    @Query(value = "SELECT COALESCE(SUM(CAST(REPLACE(NULLIF(ap.finalASRSQty, ''), ',', '.') AS DOUBLE PRECISION)), 0.0) " +
            "            FROM asrs_pallet ap WHERE ap.asrsproductionid = :arsId", nativeQuery = true)
    Double asQty(@Param("arsId") Long arsId);


    // New query: sum a text column safely by removing commas and treating empty strings as NULL
    @Query(value = "SELECT COALESCE(SUM(CAST(REPLACE(NULLIF(ap.deduction, ''), ',', '.') AS DOUBLE PRECISION)), 0.0) " +
            "FROM asrs_pallet ap WHERE ap.asrsproductionid = :arsId", nativeQuery = true)
    Double deductionQty(@Param("arsId") Long arsId);


    @Query(value = "select " +
            " gpm.bin_id as binNo, " +
            " coalesce(SUM(cast(replace(nullif(ap.allocatedQty, ''), ',', '.') as DOUBLE precision)), 0.0) as qty " +
            " from " +
            " asrs_pallet ap " +
            " left join asrs_production_detail apd on " +
            " ap.asrsproductionid = apd.id " +
            " left join gogroup_palletnumber_map gpm on " +
            " gpm.pallet_number = ap.palletnumber " +
            " left join gogroup g on " +
            " g.goname = gpm.bin_id " +
            " where " +
            " ap.asrsproductionid = apd.id " +
            " and apd.trnno =:asrsTrnNo " +
            " and g.branch =:branchId " +
            " and g.dept =:deptId " +
            " and (:type is null or g.binType =:type) " +
            " group by " +
            " gpm.bin_id", nativeQuery = true)
    List<AsrsBinList> asrsAllocatedBins(@Param("branchId") Long branchId, @Param("deptId") Long deptId,
                                        @Param("type") String type , @Param("asrsTrnNo") String asrsTrnNo);



    @Query(value = "SELECT COALESCE(SUM(CAST(REPLACE(NULLIF(ap.allocatedQty, ''), ',', '.') AS DOUBLE PRECISION)), 0.0) " +
            "    FROM asrs_pallet ap ,  asrs_production_detail apd " +
            "    WHERE ap.asrsproductionid = apd.id and apd.trnno =:asrsTrnNo", nativeQuery = true)
    Double asrsAllocatedQty(@Param("asrsTrnNo") String asrsTrnNo);

    @Query(value = "SELECT COALESCE(SUM(CAST(REPLACE(NULLIF(ap.allocatedQty, ''), ',', '.') AS DOUBLE PRECISION)), 0.0) " +
            "              FROM asrs_pallet ap " +
            "              left join  asrs_production_detail apd on ap.asrsproductionid = apd.id " +
            "              left join  gogroup_palletnumber_map gpm on gpm.pallet_number = ap.palletnumber " +
            "              WHERE ap.asrsproductionid = apd.id and apd.trnno =:asrsTrnNo and gpm.bin_id =:binName " +
            "              group by  gpm.bin_id ", nativeQuery = true)
    Double allocatedQty(@Param("asrsTrnNo") String asrsTrnNo, @Param("binName") Long binName);



    @Query(value = "  SELECT ap.* FROM asrs_pallet ap " +
            "  LEFT JOIN asrs_production_detail apd on apd.id  = ap.asrsproductionid   " +
            "                         LEFT JOIN wcshift w on w.trnno  = apd.trnno " +
            "                         left join  gogroup_palletnumber_map gpm on gpm.pallet_number = ap.palletnumber " +
            "                         WHERE ap.asrsproductionid = apd.id " +
            " and w.batchno =:batchno  and gpm.bin_id =:binName " +
            "                         and gpm.pallet_number=:palletNumber   ", nativeQuery = true)
    ASRSPalletDetails findByAsrsProductionIdAndPalletNumber(@Param("batchno") String batchno,
                                                            @Param("binName") Long binName,
                                                            @Param("palletNumber") String palletNumber);

}
