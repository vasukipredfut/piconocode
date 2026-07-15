package com.aria.service;


import com.aria.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;


@Service
public class RestService {


    @Autowired
    RestTemplate restTemplate;

    @Value("${ho.config.url}")
    private String url;

    @Value("${ho.config.items}")
    private String items;

    @Value("${ho.config.vendor}")
    private String vendor;

    @Value("${ho.config.grn}")
    private String grn;

    @Value("${ho.config.inwardinspection}")
    private String inwardInspection;





    public String saveItem(HoProductDto hoProductDto) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("F", items);
            headers.add("M", "POST");
            headers.add("P", "");

            HttpEntity entity = new HttpEntity(hoProductDto, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getBody();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    public String saveVendor(VendorDto vendorDto) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("F", vendor);
            headers.add("M", "POST");
            headers.add("P", "");

            HttpEntity entity = new HttpEntity(vendorDto, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getBody();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }


	/*
	 * public String saveHoForward(HoForwardDto hoForwardDto) {
	 * 
	 * try { HttpHeaders headers = new HttpHeaders(); headers.add("Content-Type",
	 * "application/json"); headers.add("F", grn); headers.add("M", "POST");
	 * headers.add("P", ""); System.out.println(" headers = "+headers); HttpEntity
	 * entity = new HttpEntity(hoForwardDto, headers);
	 * System.out.println(" entity = "+entity); ResponseEntity<String> response =
	 * restTemplate.postForEntity(url, entity, String.class);
	 * System.out.println(" response = "+response.getBody()); return
	 * response.getBody(); } catch (Exception ex) { ex.printStackTrace(); return
	 * ex.getMessage(); } }
	 */



    public RestResponse saveHoForward(HoForwardDto hoForwardDto) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("F", grn);
            headers.add("M", "POST");
            headers.add("P", "");

            HttpEntity entity = new HttpEntity(hoForwardDto, headers);

            ParameterizedTypeReference<RestResponse> parameterizedTypeReference = new ParameterizedTypeReference<RestResponse>() {
            };
            RestResponse response = restTemplate.postForEntity(url, entity, RestResponse.class).getBody();

            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new RestResponse();
        }
    }


    public RestResponse saveHoInwardInspection(HoInwardInspectionDto hoInwardInspectionDto) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("F", inwardInspection);
            headers.add("M", "POST" );
            headers.add("P", "");
            System.out.println(" headers = "+headers);
            HttpEntity entity = new HttpEntity(hoInwardInspectionDto, headers);
            System.out.println(" entity = "+entity);
            ParameterizedTypeReference<RestResponse> parameterizedTypeReference = new ParameterizedTypeReference<RestResponse>() {
            };
            RestResponse response = restTemplate.postForEntity(url, entity, RestResponse.class).getBody();
            System.out.println(" response = "+response);
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new RestResponse();
        }
    }
}
