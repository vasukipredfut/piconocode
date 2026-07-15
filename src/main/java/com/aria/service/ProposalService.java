package com.aria.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.aria.model.Branch;
import com.aria.model.Proposal;
import com.aria.model.ProposalQuotationFile;
import com.aria.model.ProposalSpec;
import com.aria.model.ProposalVendorSpec;
import com.aria.model.PurchaseOrder;
import com.aria.model.Specification;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.model.Vendor;

@Service
public class ProposalService {

    @Value("${proposal.quotation.file.path}")
    private String quotationuploadpath;

    @Autowired
    ProposalRepository proposalRepo;

    @Autowired
    SpecificationRepository specificationRepo;

    @Autowired
    ProposalVendorSpecRepository proposalVendorSpecRepo;

    @Autowired
    ProposalQuotationFileRepo proposalQuotationFileRepo;

    @Autowired
    ProposalSpecRepository proposalSpecRepo;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    SpecificationService specificationService;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    DateUtil dateUtil;

    public ResponseEntity<?> createProposal(ProposalDto proposalDto, User loggedUser) {

        if (proposalDto.getId() != null) {

            Optional<Proposal> optional = proposalRepo.findById(proposalDto.getId());
            if (optional.isPresent()) {
                Proposal proposal = optional.get();
                proposal.setName(proposalDto.getName());
                proposal.setCategory(proposalDto.getCategory());
                proposal.setSinglevendor(proposalDto.getSinglevendor());
                proposal.setDescription(proposalDto.getDescription());
                proposal.setUpdatedBy(loggedUser.getId());
                proposal.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                if (proposalDto.getPath() != null && proposalDto.getPath() != "") {
                    proposal.setPath(proposalDto.getPath());
                }
                Branch branch = new Branch();
                branch.setId(loggedUser.getBranch_id());
                proposal.setBranch(branch);
                proposal.setDept(loggedUser.getDepartment());
                proposal.setProposalStatus("Pending");
                proposal.setStatus("Pending");
                if (proposal.getRejectedBy() != null) {
                    proposal.setRejectedBy(null);
                    proposal.setRejected_date(null);
                }
                proposal.setActive(1L);
                if (proposalDto.getAuthorizerRole() != null) {
                    proposal.setAuthorizerRole("Plant Manager".equalsIgnoreCase(proposalDto.getAuthorizerRole()) ? 22L
                            : "Managing Director".equalsIgnoreCase(proposalDto.getAuthorizerRole()) ? 63L : null);
                }

                if (proposalDto.getUpdateProposal()) {
                    proposal.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    proposal.setCreatedBy(loggedUser.getId());
                }


                Proposal resultProposal = proposalRepo.save(proposal);

                if (Objects.nonNull(proposalDto.getProposalSpecDtos())
                        && proposalDto.getProposalSpecDtos().size() > 0) {

                    for (ProposalSpecDto proposalSpecDto : proposalDto.getProposalSpecDtos()) {

                        ProposalSpec proposalSpec = new ProposalSpec(proposalSpecDto);
                        proposalSpec.setProposal_ref_id(resultProposal.getId());
                        proposalSpec.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        proposalSpec.setCreatedBy(loggedUser.getId());
                        proposalSpec.setPacking_product(proposalSpecDto.getPacking_product());
                        proposalSpec.setSpecid(proposalSpecDto.getSpecid());
                        proposalSpec.setSpectype(proposalSpec.getSpectype());
                        ProposalSpec resultproposalSpec = proposalSpecRepo.save(proposalSpec);

                        List<ProposalVendorSpec> proposalVendorSpecs = proposalVendorSpecRepo
                                .getProposalVendorspecbyids1(proposalSpec.getProposal_ref_id(),
                                        resultproposalSpec.getId());

                        if (!proposalVendorSpecs.isEmpty() && proposalVendorSpecs != null) {
                            proposalVendorSpecRepo.deleteAll(proposalVendorSpecs);
                        }

                        for (ProposalVendorSpecDto proposalvendorDto : proposalSpecDto.getProposalVendorSpec()) {

                            ProposalVendorSpec proposalvendorSpec = new ProposalVendorSpec(proposalvendorDto);
                            proposalvendorSpec.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            proposalvendorSpec.setCreatedBy(loggedUser.getId());
                            proposalvendorSpec.setProposal_ref_id(resultProposal.getId());
                            proposalvendorSpec.setProposal_spec_id(resultproposalSpec.getId());
                            proposalvendorSpec.setStatus("Open");
                            proposalVendorSpecRepo.save(proposalvendorSpec);
                        }
                    }
                } else {
                    return new ResponseEntity<Status>(new Status("400", "Proposal Specification is Required"),
                            HttpStatus.BAD_REQUEST);
                }
                return new ResponseEntity<Status>(new Status("200", "Proposal Updated successfully!!!"), HttpStatus.OK);
            }
        }

        Proposal proposal = new Proposal(proposalDto);
        proposal.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        proposal.setCreatedBy(loggedUser.getId());
        Branch branch = new Branch();
        branch.setId(loggedUser.getBranch_id());
        proposal.setBranch(branch);
        proposal.setDept(loggedUser.getDepartment());
        proposal.setProposalStatus("Pending");
        proposal.setStatus("Pending");
        proposal.setActive(1L);
        if (proposalDto.getAuthorizerRole() != null) {
            proposal.setAuthorizerRole("Plant Manager".equalsIgnoreCase(proposalDto.getAuthorizerRole()) ? 22L
                    : "Managing Director".equalsIgnoreCase(proposalDto.getAuthorizerRole()) ? 63L : null);
        }
        Proposal resultProposal = proposalRepo.save(proposal);

        if (Objects.nonNull(proposalDto.getProposalSpecDtos()) && proposalDto.getProposalSpecDtos().size() > 0) {

            for (ProposalSpecDto proposalSpecDto : proposalDto.getProposalSpecDtos()) {

                ProposalSpec proposalSpec = new ProposalSpec(proposalSpecDto);
                proposalSpec.setProposal_ref_id(resultProposal.getId());
                proposalSpec.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                proposalSpec.setCreatedBy(loggedUser.getId());
                proposalSpec.setPacking_product(proposalSpecDto.getPacking_product());
                proposalSpec.setSpecid(proposalSpecDto.getSpecid());
                proposalSpec.setSpectype(proposalSpec.getSpectype());
                ProposalSpec resultproposalSpec = proposalSpecRepo.save(proposalSpec);

                List<ProposalVendorSpec> proposalVendorSpecs = proposalVendorSpecRepo
                        .getProposalVendorspecbyids1(proposalSpec.getProposal_ref_id(), resultproposalSpec.getId());

                if (!proposalVendorSpecs.isEmpty() && proposalVendorSpecs != null) {
                    proposalVendorSpecRepo.deleteAll(proposalVendorSpecs);
                }

                for (ProposalVendorSpecDto proposalvendorDto : proposalSpecDto.getProposalVendorSpec()) {
                    ProposalVendorSpec proposalvendorSpec = new ProposalVendorSpec(proposalvendorDto);
                    proposalvendorSpec.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    proposalvendorSpec.setCreatedBy(loggedUser.getId());
                    proposalvendorSpec.setProposal_ref_id(resultProposal.getId());
                    proposalvendorSpec.setProposal_spec_id(resultproposalSpec.getId());
                    proposalvendorSpec.setStatus("Open");

                    proposalVendorSpecRepo.save(proposalvendorSpec);
                }
            }
        } else {
            return new ResponseEntity<Status>(new Status("400", "Proposal Specification is Required"),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Status>(new Status("200", "Proposal Saved successfully!!!"), HttpStatus.OK);

    }

    public ResponseEntity<?> saveProposalVendor(List<ProposalVendorSpecDto> proposalVendorSpecDto, User loggedUser) {

        boolean firstiteration = true;
        Optional<Proposal> proposal = null;
        List<ProposalVendorSpec> proposalVendorSpecs = new ArrayList<ProposalVendorSpec>();
        for (ProposalVendorSpecDto proposalVendor : proposalVendorSpecDto) {

            if (firstiteration) {
                proposal = proposalRepo.findById(proposalVendor.getProposal_ref_id());
                firstiteration = false;
            }

            Optional<ProposalSpec> specification = proposalSpecRepo.findById(proposalVendor.getProposal_spec_id());

            Optional<Vendor> vendor = vendorRepository.findById(proposalVendor.getVendor_id());
            if (proposal.isPresent() && specification.isPresent() && vendor.isPresent()) {
                ProposalVendorSpec proposalVendorSpec = new ProposalVendorSpec(proposalVendor);
                proposalVendorSpec.setProposal_ref_id(proposal.get().getId());
                proposalVendorSpec.setProposal_spec_id(specification.get().getId());
                proposalVendorSpec.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                proposalVendorSpec.setCreatedBy(loggedUser.getId());
                proposalVendorSpec.setStatus("Open");
                proposalVendorSpecs.add(proposalVendorSpec);
            }
        }

        if (proposalVendorSpecs.size() > 0) {
            proposalVendorSpecRepo.saveAll(proposalVendorSpecs);
        }

        return new ResponseEntity<Status>(new Status("200", "Proposal Vendor Updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> ListProposal(String status, Long proposalId, Long specId, User loggedUser,
                                          Pageable pageable, Long category, Long specType,
                                          Long firmId, Long dept, Long branch) {

        Page<Proposal> listproposal = proposalRepo.getProposals(status, proposalId, specId, 1L, pageable, category, specType, firmId, dept, branch);

        if (listproposal.toList().size() > 0) {

            List<ProposalDto> proposalDtoList = listproposal.stream().map(val -> {

                ProposalDto proposalDto = new ProposalDto(val);

                Optional<User> user = userRepository.findById(proposalDto.getCreatedBy());
                if (user.isPresent()) {
                    proposalDto.setCreatedByName(user.get().getEmployeename());
                }
                if (proposalDto.getUpdatedBy() != null) {
                    Optional<User> user1 = userRepository.findById(proposalDto.getUpdatedBy());
                    if (user1.isPresent()) {
                        proposalDto.setUpdatedByName(user.get().getEmployeename());
                    }
                }

                if (proposalDto.getStatus().equalsIgnoreCase("Closed")) {
                    System.out.println("Status == " + proposalDto.getStatus());

                    System.out.println("id == " + proposalDto.getId());

                    proposalDto.setFlag(false);

                } else if (proposalDto.getCategory() == 40) {
                    System.out.println("FORMULA == " + proposalDto.getId());

                    System.out.println("id == " + proposalDto.getId());
                    List<PurchaseOrder> purchaseOrderOptional = purchaseOrderRepository.getPurchaseOrderFilterListByProposalId(proposalDto.getId());
                    if (purchaseOrderOptional.isEmpty()) {
                        proposalDto.setType("PoNotCreated");
                        proposalDto.setFlag(true);
                    } else {
                        proposalDto.setType(purchaseOrderOptional.size() + "-PoCreated");
                        proposalDto.setFlag(true);
                    }

                } else if (proposalDto.getCategory() == 39) {

                    System.out.println("NEED == " + proposalDto.getId());

                    System.out.println("id == " + proposalDto.getId());

                    List<ProposalVendorSpec> purchaseOrderOptional = proposalVendorSpecRepo.getProposalVendorSpecList(proposalDto.getId());

                    System.out.println("id vendor list == " + purchaseOrderOptional);

                    if (!purchaseOrderOptional.isEmpty()) {
                        proposalDto.setType("PoNotCreated");
                        proposalDto.setFlag(true);
                    } else {
                        proposalDto.setType("PoCreated");
                        proposalDto.setFlag(false);
                    }
                }
                return proposalDto;
            }).collect(Collectors.toList());

            PageDataDto<ProposalDto> proposalListPage = new PageDataDto<>(proposalDtoList, pageable, listproposal);

            return new ResponseEntity<PageDataDto<ProposalDto>>(proposalListPage, HttpStatus.OK);

        } else {
            return new ResponseEntity<PageDataDto<ProposalDto>>(new PageDataDto<ProposalDto>(), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> ValidateSpec(List<ValidateSpecDto> validateSpec, User loggedUser) {

        List<ProposalSpec> proposalSpecList = new ArrayList<ProposalSpec>();

        for (ValidateSpecDto validateSpecDto : validateSpec) {
            List<ProposalSpec> listproposal = proposalSpecRepo.validatespec(validateSpecDto.getSpecType(),
                    validateSpecDto.getSpecId());
            proposalSpecList.addAll(listproposal);
        }

        List<SpecMappingDto> specMappingDtos = new ArrayList<SpecMappingDto>();

        if (proposalSpecList.size() > 0) {
            Map<String, Long> maptemp = new HashMap<String, Long>();
            for (ProposalSpec proposalspec : proposalSpecList) {
                maptemp.put(proposalspec.getSpecid() + "_" + proposalspec.getSpectype(),
                        proposalspec.getProposal_ref_id());
            }

            if (maptemp.size() > 0) {
                for (Map.Entry<String, Long> entry : maptemp.entrySet()) {
                    Long key = Long.parseLong(entry.getKey().split("_")[0]);
                    Long value = entry.getValue();
                    SpecMappingDto specMappingDto = new SpecMappingDto();
                    specMappingDto.setProposalId(value);
                    specMappingDto.setSpecId(key);
                    specMappingDto.setSpecType(Long.parseLong(entry.getKey().split("_")[1]));
                    specMappingDtos.add(specMappingDto);
                }
            }

            return new ResponseEntity<List<SpecMappingDto>>(specMappingDtos, HttpStatus.OK);

        } else {
            return new ResponseEntity<List<SpecMappingDto>>(new ArrayList<SpecMappingDto>(), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> ValidateSpecWithVendor(Long specType, Long specId, Long vendorId, Long firmId) {

        List<ValidateSpec> validateSpecList = proposalSpecRepo.validateSpecWithVendor(specType, specId, vendorId, firmId);

        List<SpecMappingDto> specMappingDtos = new ArrayList<SpecMappingDto>();

        if (validateSpecList.size() > 0) {

            for (ValidateSpec validateSpec : validateSpecList) {
                SpecMappingDto specMappingDto = new SpecMappingDto();
                specMappingDto.setProposalId(validateSpec.getProposalId());
                specMappingDto.setSpecId(validateSpec.getSpecId());
                specMappingDto.setSpecType(validateSpec.getSpecType());
                specMappingDto.setVendorId(validateSpec.getVendorId());
                specMappingDto.setVendorStatus(validateSpec.getVendorStatus());
                specMappingDtos.add(specMappingDto);
            }
            return new ResponseEntity<List<SpecMappingDto>>(specMappingDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SpecMappingDto>>(new ArrayList<SpecMappingDto>(), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> getProposal(Long id, User loggedUser) {

        Optional<Proposal> proposal = proposalRepo.findById(id);

        if (proposal.isPresent()) {

            // spec based vendor list

            List<ProposalSpec> proposalSpecs = proposalSpecRepo.getProposalspec(id);
            List<ProposalSpecDto> proposalSpecDtos = new ArrayList<ProposalSpecDto>();
            List<ProposalVendorSpecDto> proposalVendorSpecDtos = new ArrayList<ProposalVendorSpecDto>();
            for (ProposalSpec proposalSpec : proposalSpecs) {

                List<ProposalVendorSpec> proposalVendorSpecs = proposalVendorSpecRepo.getProposalVendorspecbyids(proposalSpec.getProposal_ref_id(), proposalSpec.getId());

                proposalVendorSpecDtos = proposalVendorSpecs.stream().map(val -> {
                    Long vendorId = val.getVendor_id();
                    // ProposalQuotationFile proposalQuotationFile = proposalQuotationFileRepo.getProposalQuotation(id, vendorId);
                    // if (proposalQuotationFile != null)
                    // val.setPath(proposalQuotationFile.getFilename());
                    ProposalVendorSpecDto vendorSpecDto = new ProposalVendorSpecDto(val);
                    Optional<Vendor> optional = vendorRepository.findById(vendorId);
                    if (optional.isPresent()) {
                        vendorSpecDto.setVendor_name(optional.get().getName());
                        PartyList party = vendorRepository.findByVendor(optional.get().getId());
                        if (party != null) {
                            vendorSpecDto.setVendorDetails(new PartyDetails(party));
                        }

                        Double poQty = purchaseOrderRepository.findByQty(val.getProposal_ref_id(), val.getVendor_id(), proposalSpec.getSpecid());
                        if (poQty != null) {
                            vendorSpecDto.setPoCreatedQty(String.valueOf(poQty));
                        }

                    }
                    return vendorSpecDto;
                }).collect(Collectors.toList());
                ProposalSpecDto proposalSpecDto = new ProposalSpecDto(proposalSpec);
                proposalSpecDto.setSpecificationDto(specificationService.getSpecificationDetails(proposalSpecDto.getSpecid(), proposalSpecDto.getSpectype()).getBody());
                proposalSpecDto.setProposalVendorSpec(proposalVendorSpecDtos);
                proposalSpecDtos.add(proposalSpecDto);
            }

            // vendor based spec list

            List<ProposalVendorList> vendorSpeclist = proposalVendorSpecRepo.getProposalVendorList(id);

            List<ProposalVendorDTO> vendorSpecDtos = new ArrayList<ProposalVendorDTO>();

            for (ProposalVendorList proposalVendorSpec : vendorSpeclist) {

                ProposalVendorDTO vendorSpec = new ProposalVendorDTO(proposalVendorSpec);

                List<VendorSpecList> proposalSpecList = proposalSpecRepo.getVendorSpec(vendorSpec.getProposalId(),
                        vendorSpec.getVendorId());

                if (!proposalSpecList.isEmpty()) {

                    List<VendorSpecListDTO> vendorProposalSpecList = new ArrayList<>();

                    for (VendorSpecList proposalSpec : proposalSpecList) {

                        VendorSpecListDTO vendorProposalSpecDto = new VendorSpecListDTO(proposalSpec);

                        System.out.println(" spec ID = " + vendorProposalSpecDto.getSpecId());

                        Double poQty = purchaseOrderRepository.findByQty(proposalVendorSpec.getProposalId(),
                                proposalVendorSpec.getVendorId(), vendorProposalSpecDto.getSpecId());

                        if (poQty != null) {
                            vendorProposalSpecDto.setPoCreatedQty(String.valueOf(poQty));
                        }

                        vendorProposalSpecDto.setSpecificationDto(specificationService.getSpecificationDetails(
                                vendorProposalSpecDto.getSpecId(), vendorProposalSpecDto.getSpecType()).getBody());
                        vendorProposalSpecList.add(vendorProposalSpecDto);
                    }
                    vendorSpec.setVendorProposalSpec(vendorProposalSpecList);
                }
                vendorSpecDtos.add(vendorSpec);
            }

            ProposalDto proposalDto = new ProposalDto(proposal.get());
            if (proposalDto.getFirmId() != null) {
                CompanyList companyList = organisationRepository.findByFirmWithOrgAddress(proposalDto.getFirmId());
                if (companyList != null)
                    proposalDto.setCompanyDetails(new Company(companyList));
            }
            Optional<User> user = userRepository.findById(proposalDto.getCreatedBy());
            proposalDto.setCreatedByName(user.get().getEmployeename());
            if (proposalDto.getUpdatedBy() != null) {
                Optional<User> user1 = userRepository.findById(proposalDto.getUpdatedBy());
                proposalDto.setUpdatedByName(user1.get().getEmployeename());
            }
            proposalDto.setProposalSpecDtos(proposalSpecDtos);
            proposalDto.setProposalVendorDtos(vendorSpecDtos);

            return new ResponseEntity<ProposalDto>(proposalDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<ProposalDto>(new ProposalDto(), HttpStatus.OK);
        }

    }

    public SpecificationDto getProposalByProdAndSpecType(Long proId, Long specificationType) {
        return specificationService.getSpecificationDetailsByProdId(proId, specificationType);
    }

    public ResponseEntity<?> getProposalVendor(Long id, User loggedUser) {

        List<Vendor> vendorList = vendorRepository.findAllByProposaleVendor(id);

        List<VendorDto> vendorDtos = vendorList.stream().map(val -> new VendorDto(val)).collect(Collectors.toList());

        return new ResponseEntity<List<VendorDto>>(vendorDtos, HttpStatus.OK);

    }

    public ResponseEntity<?> proposalApproval(ProposalDto proposalDtos, User loggedUser) {

        Optional<Proposal> proposal = proposalRepo.findById(proposalDtos.getId());

        if (proposal.isPresent()) {
            List<ProposalSpec> proposalSpecs = new ArrayList<ProposalSpec>();
            for (ProposalSpecDto proposalSpecDto : proposalDtos.getProposalSpecDtos()) {
                Optional<ProposalSpec> proposalspec = proposalSpecRepo.findById(proposalSpecDto.getId());

                if (proposalspec.isPresent()) {
                    proposalspec.get().setApproved_price(proposalSpecDto.getApproved_price());
                    proposalSpecs.add(proposalspec.get());
                }
            }
            proposalSpecRepo.saveAll(proposalSpecs);
            proposal.get().setApprovedBy(loggedUser);
            proposal.get().setApproved_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            proposal.get().setActive(1L);
            proposal.get().setStatus("Approved");

            proposalRepo.save(proposal.get());

            return new ResponseEntity<Status>(new Status("200", "Proposal Successfully Approved"), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to Approve Proposal");
        }

    }

    // Long vendoriId
    public ResponseEntity<?> UploadFile(MultipartFile file, Long proposalId, User loggedUser) {

        Optional<Proposal> proposal = proposalRepo.findById(proposalId);

        if (proposal.isPresent()) {
            try {
                // Get the file name
                String fileName = StringUtils.cleanPath(file.getOriginalFilename() + "_" + System.currentTimeMillis());

                // Save the file to the server
                Path path = Paths.get(quotationuploadpath + "/" + fileName);
                if (!Files.exists(path)) {
                    try {
                        Files.createDirectories(path);
                        System.out.println(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle directory creation error
                    }
                }
                System.out.println(path);

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                ProposalQuotationFile proposalQuotationFile = new ProposalQuotationFile();
//                ProposalQuotationEmbedded proposalQuotationEmbedded = new ProposalQuotationEmbedded(proposalId, vendorId);
//                proposalQuotationFile.setProposalQuotationEmbedded(proposalQuotationEmbedded);
                proposalQuotationFile.setProposalId(proposalId);
                proposalQuotationFile.setFilename(fileName);
                proposalQuotationFileRepo.save(proposalQuotationFile);
                return new ResponseEntity<Status>(new Status("200", "File Uploaded Successfully"), HttpStatus.OK);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
            }

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to upload file");

        }

    }

    // Long vendorId,
    public ResponseEntity<?> downloadFile(Long proposalId, User loggedUser) throws FileNotFoundException {

        // , vendorId
        ProposalQuotationFile proposal = proposalQuotationFileRepo.findByProposalId(proposalId);
        // getProposalQuotation(proposalId);
        if (proposal != null) {

            Resource resource = loadFileAsResource(proposal.getFilename());
            String contentType = "application/octet-stream";
            /*
             * List<String> fileNameList = Arrays.asList(resource.getFilename().split("-"));
             * String fileName = fileNameList.get(0);
             */

            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Failed to download file");

        }

    }

    private Resource loadFileAsResource(String fileName) throws FileNotFoundException {
        System.out.println("#########");
        System.out.println("#########");
        System.out.println("----------");
        System.out.println("----------");
        try {
            System.out.println("fileName ==  " + fileName);
            System.out.println("----------");
            System.out.println("----------");

            Path filePath = Paths.get(quotationuploadpath + "/" + fileName);
            System.out.println("filePath uri== " + filePath.toUri());
            System.out.println("filePath fileName== " + filePath.getFileName());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new FileNotFoundException("File not found " + fileName);
        }
    }

    public ResponseEntity<?> proposalReject(Long proposalId, User user) {
        Optional<Proposal> proposal = proposalRepo.findById(proposalId);
        if (proposal.isPresent()) {
        	if(!"Rejected".equalsIgnoreCase(proposal.get().getProposalStatus())) {
            proposal.get().setProposalStatus("Rejected");
            proposal.get().setStatus("Rejected");
            proposal.get().setRejectedBy(user);
            proposal.get().setRejected_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            /*
             * if (proposal.get().getCreatedBy() != null) {
             * proposal.get().setCreatedBy(null); }
             */
            if (proposal.get().getActivatedBy() != null) {
                proposal.get().setActivatedBy(null);
                proposal.get().setActivated_date(null);
            }
            if (proposal.get().getAuthorizedBy() != null) {
                proposal.get().setAuthorizedBy(null);
                proposal.get().setAuthorized_date(null);
            }
            if (proposal.get().getApprovedBy() != null) {
                proposal.get().setApprovedBy(null);
                proposal.get().setApproved_date(null);
            }
            if (proposal.get().getVerifier1() != null) {
                proposal.get().setVerifier1(null);
                proposal.get().setVerifier1_date(null);
            }
            if (proposal.get().getVerifier2() != null) {
                proposal.get().setVerifier2(null);
                proposal.get().setVerifier2_date(null);
            }
            if (proposal.get().getVerifier3() != null) {
                proposal.get().setVerifier3(null);
                proposal.get().setVerifier3_date(null);
            }
            if (proposal.get().getValidated1() != null) {
                proposal.get().setValidated1(null);
                proposal.get().setValidated1_date(null);
            }
            if (proposal.get().getValidated2() != null) {
                proposal.get().setValidated2(null);
                proposal.get().setValidated2_date(null);
            }
            proposalRepo.save(proposal.get());
            return new ResponseEntity<Object>(new Status("200", "Proposal Rejected Sucessfully..."), HttpStatus.OK);
        	}else {
        		return new ResponseEntity<>(new Status("400", "Proposal is already Rejected!"), HttpStatus.BAD_REQUEST);
        	}
        	} else
            return ResponseEntity.badRequest().body("Proposal Id is not exist...");
    }

    public ResponseEntity<?> changeProposalStatus(Long proposalId, Optional<User> user, String status) {
        Optional<Proposal> proposal = proposalRepo.findById(proposalId);
        if (proposal.isPresent()) {
        	if (status.toLowerCase().startsWith("validated")) {

        	        if (isUserAlreadyValidateUsed(proposal.get(), user.get())) {
        	            return new ResponseEntity<>(
        	                new Status("400", "Same user cannot validate multiple times on this proposal!"),
        	                HttpStatus.BAD_REQUEST
        	            );
        	        }
        	    }
           
            	if (status.toLowerCase().startsWith("verified")) {

            	        if (isUserAlreadyUsed(proposal.get(), user.get())) {
            	            return new ResponseEntity<>(
            	                new Status("400", "Same user cannot verified multiple times! on this proposal!"),
            	                HttpStatus.BAD_REQUEST
            	            );
            	        }
            	    }
            if (status.equalsIgnoreCase("approved") ) {
            	if("validated2".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("Approved");
                proposal.get().setApprovedBy(user.get());
                proposal.get().setApproved_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                proposal.get().setStatus("Approved");
            	}else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already " + status + " or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}
            }
            if (status.equalsIgnoreCase("authorized")) {
            	if("Approved".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("Authorized");
                proposal.get().setAuthorizedBy(user.get());
                proposal.get().setAuthorized_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                proposal.get().setStatus("Authorized");
            }else {
            	return new ResponseEntity<>(new Status("400", "Proposal is already" + status + " or invalid action type!"), HttpStatus.BAD_REQUEST);
          	}
            }
            if (status.equalsIgnoreCase("activated")) {
            	if("Pending".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("Activated");
                proposal.get().setActivatedBy(user.get());
                proposal.get().setActivated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                proposal.get().setStatus("Activated");
                }else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already" + status + "or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}}
            if (status.equalsIgnoreCase("verified1")) {
            	if("Activated".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("verified1");
                proposal.get().setVerifier1(user.get());
                proposal.get().setVerifier1_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            	}else if("verified1".equalsIgnoreCase(proposal.get().getProposalStatus())){
                        proposal.get().setProposalStatus("verified2");
                        proposal.get().setVerifier2(user.get());
                        proposal.get().setVerifier2_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                }else if("verified2".equalsIgnoreCase(proposal.get().getProposalStatus())){
                            proposal.get().setProposalStatus("verified3");
                            proposal.get().setStatus("Verified");
                            proposal.get().setVerifier3(user.get());
                            proposal.get().setVerifier3_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                } else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already verified by 3 person or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}    
            }
            if (status.equalsIgnoreCase("verified2")) {
            	if("verified1".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("verified2");
                proposal.get().setVerifier2(user.get());
                proposal.get().setVerifier2_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            	} else if("verified2".equalsIgnoreCase(proposal.get().getProposalStatus())){
                    proposal.get().setProposalStatus("verified3");
                    proposal.get().setStatus("Verified");
                    proposal.get().setVerifier3(user.get());
                    proposal.get().setVerifier3_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            	}else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already verified by 3 person or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}
            }
            if (status.equalsIgnoreCase("verified3")) {
            	if("verified2".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("verified3");
                proposal.get().setStatus("Verified");
                proposal.get().setVerifier3(user.get());
                proposal.get().setVerifier3_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            	} else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already verified by 3 person or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}
            }
            if (status.equalsIgnoreCase("validated1")) {
            	if("verified3".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("validated1");
                proposal.get().setValidated1(user.get());
                proposal.get().setValidated1_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            	} else if("validated1".equalsIgnoreCase(proposal.get().getProposalStatus())){
                    proposal.get().setProposalStatus("validated2");
                    proposal.get().setValidated2(user.get());
                    proposal.get().setValidated2_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    proposal.get().setStatus("Validated");
                	}else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already velidated by 2 person or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}
            }
            if (status.equalsIgnoreCase("validated2")) {
            	if("validated1".equalsIgnoreCase(proposal.get().getProposalStatus())){
                proposal.get().setProposalStatus("validated2");
                proposal.get().setValidated2(user.get());
                proposal.get().setValidated2_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                proposal.get().setStatus("Validated");
            	} else {
                	return new ResponseEntity<>(new Status("400", "Proposal is already validated by 2 person or invalid action type!"), HttpStatus.BAD_REQUEST);
              	}
            }
            proposalRepo.save(proposal.get());
            return new ResponseEntity<Object>(new Status("200", "Proposal Status Updated Sucessfully ..."),
                    HttpStatus.OK);
        } else
            return ResponseEntity.badRequest().body("Proposal Id is not exist...");
    }

    
    private boolean isUserAlreadyUsed(Proposal proposal, User user) {
        Long userId = user.getId();

        return (proposal.getVerifier1() != null && proposal.getVerifier1().getId().equals(userId)) ||
               (proposal.getVerifier2() != null && proposal.getVerifier2().getId().equals(userId)) ||
               (proposal.getVerifier3() != null && proposal.getVerifier3().getId().equals(userId));
    }
    private boolean isUserAlreadyValidateUsed(Proposal proposal, User user) {
        Long userId = user.getId();

        return (proposal.getValidated1() != null && proposal.getValidated1().getId().equals(userId)) ||
               (proposal.getValidated2() != null && proposal.getValidated2().getId().equals(userId));
    }
    public ResponseEntity<?> closeProposal(Long proposalId, Long proposalSpecId, Long proposalSpecIdVendorId,
                                           String type, boolean active, String status) {

        if (type.equalsIgnoreCase("specVendor")) {
            Optional<ProposalVendorSpec> optionalProposalVendorSpec = proposalVendorSpecRepo
                    .findById(proposalSpecIdVendorId);
            if (optionalProposalVendorSpec.isPresent()) {
                ProposalVendorSpec proposalVendorSpec = optionalProposalVendorSpec.get();
                proposalVendorSpec.setActive(active);
                proposalVendorSpecRepo.save(proposalVendorSpec);
            }
            return new ResponseEntity<Object>(new Status("200", "Proposal Spec Vendor  Status Updated Sucessfully ..."),
                    HttpStatus.OK);

        } else if (type.equalsIgnoreCase("spec")) {

            Optional<ProposalSpec> proposalSpecOptional = proposalSpecRepo.findById(proposalSpecId);
            if (proposalSpecOptional.isPresent()) {

                List<ProposalVendorSpec> proposalVendorSpecList = proposalVendorSpecRepo
                        .getProposalVendorbyids(proposalId, proposalSpecId);

                for (ProposalVendorSpec proposalVendorSpec : proposalVendorSpecList) {
                    proposalVendorSpec.setActive(active);
                    proposalVendorSpecRepo.save(proposalVendorSpec);
                }

                ProposalSpec proposalSpec = proposalSpecOptional.get();
                proposalSpec.setActive(active);
                proposalSpecRepo.save(proposalSpec);

            }

            return new ResponseEntity<Object>(new Status("200", "Proposal Spec Status Updated Sucessfully ..."),
                    HttpStatus.OK);

        } else if (type.equalsIgnoreCase("proposal")) {

            Optional<Proposal> optionalProposal = proposalRepo.findById(proposalId);
            if (optionalProposal.isPresent()) {
                Proposal proposal = optionalProposal.get();
                if (active) {
                    proposal.setStatus(status);
                } else {
                    proposal.setStatus("Closed");
                }
                proposalRepo.save(proposal);

            }
            return new ResponseEntity<Object>(new Status("200", "Proposal Status Updated Sucessfully ..."),
                    HttpStatus.OK);

        } else {
            return ResponseEntity.badRequest().body("Proposal Id is not exist...");
        }
    }

    public ResponseEntity<?> updateProposalVendorBySpec(List<VendorSpecDto> proposalVendorSpecDto, User loggedUser) {

        if (!proposalVendorSpecDto.isEmpty()) {

            for (VendorSpecDto proposalVendor : proposalVendorSpecDto) {

                List<ProposalVendorSpec> proposalVendorSpecsList = proposalVendorSpecRepo
                        .getProposalVendor(proposalVendor.getProposalId(), proposalVendor.getVendorId());
                if (!proposalVendorSpecsList.isEmpty()) {
                    for (ProposalVendorSpec proposalVendorSpec : proposalVendorSpecsList) {
                        proposalVendorSpec.setExpiry_date(dateUtil.getStringTODateWk(proposalVendor.getExpiryDate()));
                        proposalVendorSpec.setPo_validity_request(proposalVendor.getPoValidityRequest());
                        proposalVendorSpecRepo.save(proposalVendorSpec);
                    }
                }

                if (!proposalVendor.getVendorProposalSpec().isEmpty()) {

                    for (VendorProposalSpecDto vendorProposalSpecDto : proposalVendor.getVendorProposalSpec()) {

                        Optional<ProposalVendorSpec> optional = proposalVendorSpecRepo.getProposalVendorSpecBySpecId(
                                vendorProposalSpecDto.getProposalId(), vendorProposalSpecDto.getProposalSpecId(),
                                proposalVendor.getVendorId());

                        if (optional.isPresent()) {
                            ProposalVendorSpec proposalVendorSpec = optional.get();
                            proposalVendorSpec.setNet_approved_price(vendorProposalSpecDto.getNetApprovedPrice());
                            proposalVendorSpec.setNet_approved_qty(vendorProposalSpecDto.getNetApprovedQty());
                            proposalVendorSpec.setLot(vendorProposalSpecDto.getLot());
                            proposalVendorSpec.setUpdatedBy(loggedUser.getId());
                            proposalVendorSpec.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            proposalVendorSpecRepo.save(proposalVendorSpec);
                        }
                    }
                }
            }
            return new ResponseEntity<Status>(new Status("200", "Proposal Vendor Updated successfully!!!"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Proposal Vendor Not Updated!!!"), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> closeVendorSpec(List<CloseVendorSpec> closeVendorSpecList, User loggedUSer) {

        if (!closeVendorSpecList.isEmpty()) {
            for (CloseVendorSpec closeVendorSpec : closeVendorSpecList) {

                if (!closeVendorSpec.getProposalSpecId().isEmpty()) {

                    for (Long proposalSpecId : closeVendorSpec.getProposalSpecId()) {

                        Optional<ProposalVendorSpec> optional = proposalVendorSpecRepo.getProposalVendorSpecBySpecId(
                                closeVendorSpec.getProposalId(), proposalSpecId, closeVendorSpec.getVendorId());
                        if (optional.isPresent()) {
                            ProposalVendorSpec proposalVendorSpec = optional.get();
                            proposalVendorSpec.setActive(closeVendorSpec.isActive());
                            proposalVendorSpecRepo.save(proposalVendorSpec);
                        }
                    }

                } else {
                    return ResponseEntity.badRequest().body(" ProposalSpecId Id is not exist...");
                }
            }
            return new ResponseEntity<Object>(new Status("200", "Proposal Status Updated Sucessfully ..."),
                    HttpStatus.OK);
        } else {
            return ResponseEntity.badRequest().body("Proposal Id is not exist...");
        }
    }


    public ResponseEntity<?> deleteProposalSpec(Long proposalId, Long proposalSpecId) {

        Optional<ProposalSpec> proposalSpecOptional = proposalSpecRepo.findById(proposalSpecId);
        if (proposalSpecOptional.isPresent()) {
            List<ProposalVendorSpec> proposalVendorSpecList = proposalVendorSpecRepo.getProposalVendorbyids(proposalId, proposalSpecId);
            if (!proposalVendorSpecList.isEmpty()) {
                proposalVendorSpecRepo.deleteAll(proposalVendorSpecList);
            }
            proposalSpecRepo.delete(proposalSpecOptional.get());
            return new ResponseEntity<Object>(new Status("200", "Proposal Spec deleted Successfully !..."), HttpStatus.OK);
        }

        return new ResponseEntity<Object>(new Status("400", "Proposal Spec not deleted ..."), HttpStatus.OK);

    }

}