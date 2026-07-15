package com.aria.dao;

import com.aria.dto.QCKpiListDto;
import com.aria.model.InstruLog;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class CommonDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public Session getSession() {
        return entityManager.unwrap(Session.class);
    }


    public List<Map<String, Object>> findQcInProcess(QCKpiListDto step) {
        StringBuilder query = new StringBuilder("SELECT * FROM qc_inprocess q WHERE  ");
        List<Object> paramList = new ArrayList<Object>();
        query.append(" q.batchno=?  ");
        paramList.add(step.getBatchNo());
        List<Map<String, Object>> kpiDetailsList = null;
        try {
            kpiDetailsList = jdbcTemplate.queryForList(query.toString(),
                    paramList.toArray(new Object[paramList.size()]));

            for (Map<String, Object> kpiDetailsListout : kpiDetailsList) {
                System.out.println(kpiDetailsListout);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kpiDetailsList;
    }

    public List<Map<String, Object>> getLogWithDate(Long id, Date date1, Date date2) {
        StringBuilder query = new StringBuilder("SELECT DISTINCT date, enrollnumber  FROM instrulog i WHERE");
        List<Object> paramList = new ArrayList<Object>();
        query.append(" i.enrollnumber=?  AND ( i.date BETWEEN ? AND ? )");
        paramList.add(id);
        paramList.add(date1);
        paramList.add(date2);
        List<Map<String, Object>> kpiDetailsList = null;
        try {
            kpiDetailsList = jdbcTemplate.queryForList(query.toString(),
                    paramList.toArray(new Object[paramList.size()]));

            for (Map<String, Object> kpiDetailsListout : kpiDetailsList) {
                System.out.println(kpiDetailsListout);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kpiDetailsList;
    }


    public List<Map<String, Object>> getUserWithDate(Date date) {
        StringBuilder query = new StringBuilder("SELECT id, employeename , email, staffid  FROM employee_details WHERE bioid IN (SELECT DISTINCT enrollnumber FROM instrulog  WHERE  date=? )");
        List<Object> paramList = new ArrayList<Object>();
        paramList.add(date);
        List<Map<String, Object>> kpiDetailsList = null;
        try {
            kpiDetailsList = jdbcTemplate.queryForList(query.toString(),
                    paramList.toArray(new Object[paramList.size()]));

            for (Map<String, Object> kpiDetailsListout : kpiDetailsList) {
                System.out.println(kpiDetailsListout);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kpiDetailsList;
    }


}
