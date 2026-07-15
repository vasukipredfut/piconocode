package com.aria.service;

import com.aria.dao.OrganisationRepository;
import com.aria.model.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService {

    @Autowired
    OrganisationRepository organisationRepository;

    public void save(String orgName){
        Organisation organisation = new Organisation();
        organisation.setName(orgName);
        organisationRepository.save(organisation);
    }
}
