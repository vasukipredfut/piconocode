package com.aria.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.DepayMasterRepository;
import com.aria.dao.EpayMasterRepository;
import com.aria.dto.MyCellDto;
import com.aria.model.DepayMaster;
import com.aria.model.EpayMaster;

@Component
public class ExcelUtil {

	@Autowired
	private EpayMasterRepository epayMasterRepository;

	@Autowired
	private DepayMasterRepository depayMasterRepository;

	public static boolean checkExcelFormat(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			return true;
		}
		return false;
	}

	public static Map<Integer, List<MyCellDto>> readExcel(InputStream inputStream, String fileLocation)
			throws IOException {

		Map<Integer, List<MyCellDto>> data = new HashMap<>();

		if (fileLocation.endsWith(".xls")) {
			data = readHSSFWorkbook(inputStream);
		} else if (fileLocation.endsWith(".xlsx")) {
			data = readXSSFWorkbook(inputStream);
		}

		int maxNrCols = data.values().stream().mapToInt(List::size).max().orElse(0);

		data.values().stream().filter(ls -> ls.size() < maxNrCols).forEach(ls -> {
			IntStream.range(ls.size(), maxNrCols).forEach(i -> ls.add(new MyCellDto("")));
		});

		return data;
	}

	private static Map<Integer, List<MyCellDto>> readHSSFWorkbook(InputStream inputStream) throws IOException {
		Map<Integer, List<MyCellDto>> data = new HashMap<>();
		HSSFWorkbook workbook = null;
		try {
			workbook = new HSSFWorkbook(inputStream);
			HSSFSheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				HSSFRow row = sheet.getRow(i);
				data.put(i, new ArrayList<>());
				if (row != null) {
					for (int j = 0; j < row.getLastCellNum(); j++) {
						HSSFCell cell = row.getCell(j);
						CellValue cellValue = evaluator.evaluate(cell);
						if (cellValue != null) {
							MyCellDto myCell = new MyCellDto();
							myCell.setContent(readCellContent(cellValue));
							data.get(i).add(myCell);
						} else {
							MyCellDto myCell = new MyCellDto();
							String t = "";
							myCell.setContent(t);
							data.get(i).add(myCell);
						}
					}
				}
			}
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}

		return data;
	}

	private static String readCellContent(CellValue cellValue) {
		String content;
		switch (cellValue.getCellType()) {
		case STRING:
			content = cellValue.getStringValue();
			break;
		case NUMERIC:
			content = cellValue.getNumberValue() + "";
			break;
		case BOOLEAN:
			content = cellValue.getBooleanValue() + "";
			break;
		default:
			content = "";
		}
		return content;
	}

	private static Map<Integer, List<MyCellDto>> readXSSFWorkbook(InputStream inputStream) throws IOException {
		XSSFWorkbook workbook = null;
		Map<Integer, List<MyCellDto>> data = new HashMap<>();
		try {
			workbook = new XSSFWorkbook(inputStream);
			XSSFSheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

			for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
				XSSFRow row = sheet.getRow(i);
				data.put(i, new ArrayList<>());
				if (row != null) {
					for (int j = 0; j < row.getLastCellNum(); j++) {
						XSSFCell cell = row.getCell(j);
						CellValue cellValue = evaluator.evaluate(cell);
						if (cellValue != null) {
							MyCellDto myCell = new MyCellDto();
							myCell.setContent(readCellContent(cellValue));

							data.get(i).add(myCell);
						} else {
							MyCellDto myCell = new MyCellDto();
							String t = "";
							myCell.setContent(t);
							data.get(i).add(myCell);
						}

					}
				}
			}
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}
		return data;
	}

	public List<EpayMaster> convertExcelToListOfUpdateStaff(Map<Integer, List<MyCellDto>> data) {
		List<EpayMaster> employeeList = new ArrayList<>();

		try {
			for (int i = 1; i < data.size(); i++) {
				EpayMaster employee = new EpayMaster();
				List<MyCellDto> row = data.get(i);
				employee = setEpayObjectUpdate(row);
				if (employee != null)
					employeeList.add(employee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeeList;
	}

	public EpayMaster setEpayObjectUpdate(List<MyCellDto> row) throws ParseException {
		EpayMaster employee = null;
		if (row.get(1).getContent() != "") {
			employee = epayMasterRepository.findByEmpcode((Long) Math.round(Double.parseDouble(row.get(1).getContent())));
			if(employee!=null) {
				employee.setEmpcode((Long) Math.round(Double.parseDouble(row.get(1).getContent())));
				if (row.get(0).getContent() != "")
					employee.setName(row.get(0).getContent());
				if (row.get(2).getContent() != "")
					employee.setTdeduction((int) Math.round(Double.parseDouble(row.get(2).getContent())));
				if (row.get(3).getContent() != "")
					employee.setNetsalary((int) Math.round(Double.parseDouble(row.get(3).getContent())));
				if (row.get(4).getContent() != "")
					employee.setBlsalary((int) Math.round(Double.parseDouble(row.get(4).getContent())));
				if (row.get(5).getContent() != "")
					employee.setFlsalary((int) Math.round(Double.parseDouble(row.get(5).getContent())));
				if (row.get(6).getContent() != "")
					employee.setLsalary((int) Math.round(Double.parseDouble(row.get(6).getContent())));
				if (row.get(7).getContent() != "")
					employee.setAlsalary((int) Math.round(Double.parseDouble(row.get(7).getContent())));
				if (row.get(8).getContent() != "")
					employee.setPerdayamount(Double.parseDouble(row.get(8).getContent()));
			}
		}
		
		return employee;
	}

	public List<DepayMaster> convertExcelToListOfUpdateEmployee(Map<Integer, List<MyCellDto>> data) {
		List<DepayMaster> employeeList = new ArrayList<>();

		try {
			for (int i = 1; i < data.size(); i++) {
				DepayMaster employee = new DepayMaster();
				List<MyCellDto> row = data.get(i);
				employee = setDepayObjectUpdate(row);
				if (employee != null)
					employeeList.add(employee);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return employeeList;
	}

	public DepayMaster setDepayObjectUpdate(List<MyCellDto> row) throws ParseException {
		DepayMaster employee = null;
		if (row.get(0).getContent() != "") {
			employee = depayMasterRepository.findByCode(Math.round(Double.parseDouble(row.get(0).getContent())) + "");
			if (employee != null) {
				Long code = (Long) Math.round(Double.parseDouble(row.get(0).getContent()));
				employee.setCode(code + "");
				if (row.get(1).getContent() != "")
					employee.setName(row.get(1).getContent());
				if (row.get(2).getContent() != "")
					employee.setBasic((int) Math.round(Double.parseDouble(row.get(2).getContent())));
				if (row.get(3).getContent() != "")
					employee.setEsiceil((int) Math.round(Double.parseDouble(row.get(3).getContent())));
				if (row.get(4).getContent() != "")
					employee.setEpfbase((int) Math.round(Double.parseDouble(row.get(4).getContent())));
				if (row.get(5).getContent() != "")
					employee.setBank(row.get(5).getContent());
				if (row.get(6).getContent() != "")
					employee.setBankacno(row.get(6).getContent());
				if (row.get(7).getContent() != "")
					employee.setBank_ifsc(row.get(7).getContent());
			}
		}
		return employee;
	}
}