package com.aria.utils;

import com.aria.dao.SalaryStructureRepo;
import com.aria.dto.CwagesStructureList;
import com.aria.model.Cwages;
import com.aria.model.SalaryStructure;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CSVHelperUtil {


    @Autowired
    SalaryStructureRepo salaryStructureRepo;

    public static String TYPE = "text/csv";
    static String[] HEADERs = {"ID","TRN_YEAR", "DESIGNATION", "DESIGNATIONID", "APPLICABLE_TO",  "BASIC", "DA", "BASIC_DA_PERCENTAGE",  "FIXED_ALLOWANCE",
            "GOOD_WORK", "CONVEYANCE", "MINMUM_CONVEYANCE", "MAXIMUM_CONVEYANCE", "TOTAL_SALARY_TYPE",  "COMPANY_ESI", "DEDUCTION_ESI",
            "ESI_CEILING", "COMPANY_EPF", "COMPANY_FPF", "DEDUCTION_FPF", "EPF_BASE",  "MINMUM_HRA","STATUS" };


    static String[] CWAGESIMPORTHEADERs = {"ID", "CODENO", "TRN_YEAR", "DESIGNATION", "SALSTRUCTUREID", "NAME", "AMOUNT", "JOURNAL",
            "MATERIALMOVEMENT", "TRANSPORT", "TYPE", "SLAB", "ACTIVITY"};

    public static boolean hasCSVFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }



    public String checkUploadSalaryStructure(InputStream is) {

        String message = null;

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader(HEADERs).withIgnoreHeaderCase(true).withTrim())) {

            List<CSVRecord> csvRecords = csvParser.getRecords();

            int count = csvRecords.size();

            if (count > 1) {
                message = "success";
            } else {
                message = "empty";
            }


        } catch (IOException e) {
            e.printStackTrace();
            message = " fail to parse CSV file: " + e.getMessage();
        }

        return message;
    }





    public List<SalaryStructure> uploadSalaryStructure(InputStream is) {

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader(HEADERs).withDelimiter(',').withIgnoreHeaderCase(true).withTrim())) {

            List<SalaryStructure> salaryStructureList = new ArrayList<SalaryStructure>();

            List<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {

                Long recNo =  csvRecord.getRecordNumber();

                if(recNo == 1){
                    System.out.println("HEADER  === " + csvRecord);
                } else {
                	System.out.println("CSVRecord  === " + csvRecord);
                	
                    SalaryStructure salaryStructure = new SalaryStructure(csvRecord);

                    salaryStructureList.add(salaryStructure);
                }

            }
            return salaryStructureList;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(" fail to parse CSV file: " + e.getMessage());
        }
    }


    public void writeSalaryStructureToCsv(Writer writer, List<SalaryStructure> salaryStructureList) {


        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("ID","TRN_YEAR", "DESIGNATION", "DESIGNATIONID", "APPLICABLE_TO",  "BASIC", "DA", "BASIC_DA_PERCENTAGE",  "FIXED_ALLOWANCE",
                    "GOOD_WORK", "CONVEYANCE", "MINMUM_CONVEYANCE", "MAXIMUM_CONVEYANCE", "TOTAL_SALARY_TYPE",  "COMPANY_ESI", "DEDUCTION_ESI",
                    "ESI_CEILING", "COMPANY_EPF", "COMPANY_FPF", "DEDUCTION_FPF", "EPF_BASE",  "MINMUM_HRA","STATUS");
            for (SalaryStructure salStruct : salaryStructureList) {
                csvPrinter.printRecord(salStruct.getAid(), salStruct.getTrnyear(), salStruct.getName(),   salStruct.getId(), salStruct.getApplyto(),
                        salStruct.getMinbasic(), salStruct.getDa(), salStruct.getBper(), salStruct.getAllowance(), salStruct.getGwper(), salStruct.getConvper(),
                        salStruct.getMinconv(), salStruct.getMaxconv(), salStruct.getFixed(), salStruct.getEesiper(), salStruct.getDesiper(),  salStruct.getEsiceil(),
                        salStruct.getEepf1per(), salStruct.getEepf2per(), salStruct.getDepfper(), salStruct.getEpfbase(), salStruct.getMinhra(), salStruct.getStatus());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error While writing CSV " + e.getMessage());
        }
    }

    public void writeSalaryStructureToCsv(Writer writer) {

        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("ID","TRN_YEAR", "DESIGNATION", "DESIGNATIONID", "APPLICABLE_TO",  "BASIC", "DA", "BASIC_DA_PERCENTAGE",  "FIXED_ALLOWANCE",
                    "GOOD_WORK", "CONVEYANCE", "MINMUM_CONVEYANCE", "MAXIMUM_CONVEYANCE", "TOTAL_SALARY_TYPE",  "COMPANY_ESI", "DEDUCTION_ESI",
                    "ESI_CEILING", "COMPANY_EPF", "COMPANY_FPF", "DEDUCTION_FPF", "EPF_BASE",  "MINMUM_HRA","STATUS");
        } catch (IOException e) {
            throw new RuntimeException("Error While writing CSV " + e.getMessage());
        }
    }


    public String checkUploadCwagesSalaryStructure(InputStream is) {

        String message = null;

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader(CWAGESIMPORTHEADERs).withIgnoreHeaderCase(true).withTrim())) {

            List<CSVRecord> csvRecords = csvParser.getRecords();

            int count = csvRecords.size();

            if (count > 1) {
                message = "success";
            } else {
                message = "empty";
            }


        } catch (IOException e) {
            e.printStackTrace();
            message = " fail to parse CSV file: " + e.getMessage();
        }

        return message;
    }


    public List<Cwages> uploadCwagesSalaryStructure(InputStream is) {

        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));


             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withHeader(CWAGESIMPORTHEADERs).withIgnoreHeaderCase(true).withTrim())) {


            List<Cwages> cwages = new ArrayList<Cwages>();

            List<CSVRecord> csvRecords = csvParser.getRecords();

            int count = csvRecords.size();

            System.out.println("count === " + count);
            
            for (CSVRecord csvRecord : csvRecords) {

                Long recNo =  csvRecord.getRecordNumber();
                

                if(recNo == 1){
                    System.out.println("HEADER  === " + csvRecord);
                } else {
                	System.out.println("CSVRecord  === " + csvRecord);
                    Cwages salaryStructure = new Cwages(csvRecord);
                    String name = csvRecord.get("DESIGNATION");
                    System.out.println(name);
                    SalaryStructure structure = findBy(name);
                    if (structure != null) {
                        salaryStructure.setSalstruid(structure.getId());
                    }
                    cwages.add(salaryStructure);
                }
            }
            return cwages;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(" fail to parse CSV file: " + e.getMessage());
        }
    }


    public SalaryStructure findBy(String name) {
        Optional<SalaryStructure> salaryStructure1 = salaryStructureRepo.findByName(name);
        if (salaryStructure1.isPresent()) {
            return salaryStructure1.get();
        } else {
            return null;
        }
    }


    public void writeCwagesSalaryStructureToCsv(Writer writer, List<CwagesStructureList> salaryStructureList) {


        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("ID", "CODENO", "TRN_YEAR", "DESIGNATION", "SALSTRUCTUREID", "NAME", "AMOUNT", "JOURNAL",
                    "MATERIALMOVEMENT", "TRANSPORT", "TYPE", "SLAB", "ACTIVITY");
            for (CwagesStructureList salStruct : salaryStructureList) {
                csvPrinter.printRecord(salStruct.getId(), salStruct.getCodeNo(), salStruct.getTrnYear(), salStruct.getDesignation(), salStruct.getSalStructId(),
                        StringEscapeUtils.unescapeJava(salStruct.getName()), salStruct.getAmount(), salStruct.getJournal(), salStruct.getMaterialMovement(),
                        salStruct.getTransport(), salStruct.getType(), salStruct.getSlab(), salStruct.getActivity());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error While writing CSV " + e.getMessage());
        }
    }


    public void writeCwagesSalaryStructureHEARDSToCsv(Writer writer) {


        try (CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("ID", "CODENO", "TRN_YEAR", "DESIGNATION", "SALSTRUCTUREID", "NAME", "AMOUNT", "JOURNAL",
                    "MATERIALMOVEMENT", "TRANSPORT", "TYPE", "SLAB", "ACTIVITY");
        } catch (IOException e) {
            throw new RuntimeException("Error While writing CSV " + e.getMessage());
        }
    }
}
