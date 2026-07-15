package com.aria.dao;

import com.aria.dto.PartyList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Vendor;
import com.aria.model.VendorList;

import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByName(String name);

    @Query(value = "SELECT * FROM ledger WHERE  LOWER(name) LIKE  LOWER(:name)", nativeQuery = true)
    List<Vendor> findAllByVendorByName(@Param("name") String name);
    
    
    @Query(value = "SELECT * FROM ledger order by name asc", nativeQuery = true)
    List<Vendor> findAll();
    
    @Query(value = "SELECT l.* , su.name as partygroupName  FROM ledger l left join suppliergroup su on su.id=l.partygroupid order by name asc", nativeQuery = true)
    List<VendorList> findAllwithGroupName();
    
    @Query(value = "SELECT l.* FROM ledger l, proposal_vendor_spec pv WHERE l.id=pv.vendor_id and pv.proposal_ref_id=:id", nativeQuery = true)
    List<Vendor> findAllByProposaleVendor(@Param("id") Long proposalId);


    @Query(value = "SELECT * FROM ledger ld WHERE "
            + " (:vendorName is null or lower(ld.name) like ('%' || lower(:vendorName) || '%')) "
            + " AND (:groupId is null or ld.partygroupid=:groupId ) "
            + " AND (:gstNo is null or lower(ld.gst) like ('%' || lower(:gstNo) || '%')) "
            + " AND (:mobileNo is null or lower(ld.mobile) like ('%' || lower(:mobileNo) || '%')) "
            + " order by id desc", nativeQuery = true)
    Page<Vendor> findAll(@Param("vendorName") String vendorName, @Param("groupId") Long groupId, @Param("gstNo") String gstNo, @Param("mobileNo") String mobileNo, Pageable pageable);


    @Query(value = "SELECT ld.id, ld.name FROM ledger ld  WHERE "
               + " (:vendorName is null or lower(ld.name) like ('%' || lower(:vendorName) || '%')) "
               + " AND (:gst is null or lower(ld.gst) like ('%' || lower(:gst) || '%')) ", nativeQuery = true)
    List<Tuple> findAlls(@Param("vendorName") String vendorName, @Param("gst") String gst);

    @Query(value = "SELECT l.* , su.name as partygroupName  FROM ledger l left join suppliergroup su on su.id=l.partygroupid order by name asc", nativeQuery = true)
    Page<VendorList> findAllWithGroupName(Pageable pageable);

    @Query(value = "SELECT l.* , su.name as partygroupName  FROM ledger l left join suppliergroup su on su.id=l.partygroupid WHERE "
            + " (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%'))  order by name asc", nativeQuery = true)
    List<VendorList> findAllWithGroupName(@Param("vendorName") String vendorName);


    @Query(value = "SELECT l.id as id, l.name as name, l.address as address, l.city as city , l.pincode as pinCode , l.gst as GST , " +
            " l.email as email, l.phone as phoneNo , l.state as state, l.mobile as mobileNo FROM ledger l WHERE  l.id =:id", nativeQuery = true)
    PartyList findByVendor(@Param("id") Long id);

}
