package com.aria.dao;

import java.util.List;

import com.aria.dto.SpecificationParameterList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Specification_parameter_mapping;

@Repository
public interface SpecificationParameterMappingRepository extends JpaRepository<Specification_parameter_mapping, Long> {

	@Query(value = "SELECT *  FROM specification_parameter_mapping s WHERE s.specification_id =:id and s.active=1 order by s.parameter_id, parameter_option_type", nativeQuery = true)
    List<Specification_parameter_mapping> findBySpecificationId (@Param("id") Long id);


    @Query(value = "SELECT p.id as id, p.name as name , s.specification_type as specification_type , sp.parameter_type as parameter_type, sp.parameter_option_type as paramter_option_type, sp.active as active, s.branch as branch, s.department as department, s.organisation as organisation FROM specification_parameter_mapping sp LEFT JOIN specification s ON s.id=sp.specification_id LEFT JOIN specification_parameter p ON p.id = sp.parameter_id WHERE s.id =:id and sp.active=1 group by p.id , p.name, s.specification_type  , sp.parameter_type , sp.parameter_option_type , sp.active , s.branch , s.department , s.organisation order by p.id, sp.parameter_option_type", nativeQuery = true)
    List<SpecificationParameterList> findBySpecification (@Param("id") Long id);

    
    @Query(value = "SELECT *  FROM specification_parameter_mapping s WHERE s.specification_id =:specid and s.parameter_id =:paramid and s.active=1", nativeQuery = true)
    List<Specification_parameter_mapping> findBySpecification (@Param("specid") Long specid, @Param("paramid") Long paramid);



	
}
