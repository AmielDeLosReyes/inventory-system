package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelExportService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public ByteArrayInputStream exportCombinedExcel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        createInventorySheet(workbook);
        createSalesSummarySheet(workbook);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void createInventorySheet(XSSFWorkbook workbook) {
        List<String> productNames = inventoryRepository.findAllUniqueProductNames();

        for (String productName : productNames) {
            List<Inventory> inventories = inventoryRepository.findByProductName(productName);

            // Create Inventory sheet
            XSSFSheet inventorySheet = workbook.createSheet(productName + " Inventory");
            Row headerRow = inventorySheet.createRow(0);
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Description");
            headerRow.createCell(2).setCellValue("Cost");
            headerRow.createCell(3).setCellValue("Quantity");
            headerRow.createCell(4).setCellValue("In Amount");
            headerRow.createCell(5).setCellValue("Out Amount");
            headerRow.createCell(6).setCellValue("Balance");
            headerRow.createCell(7).setCellValue("Remarks");

            int rowNum = 1;
            for (Inventory inventory : inventories) {
                Row row = inventorySheet.createRow(rowNum++);
                LocalDate entryDate = null;
                try {
                    entryDate = LocalDate.parse(inventory.getEntryDate());
                } catch (DateTimeParseException e) {
                    // Handle invalid date format
                }
                row.createCell(0).setCellValue(entryDate != null ? entryDate.toString() : "");
                row.createCell(1).setCellValue(inventory.getDescription());
                row.createCell(2).setCellValue(inventory.getCost() != null ? inventory.getCost().toString() : "");
                row.createCell(3).setCellValue(inventory.getQuantity());
                row.createCell(4).setCellValue(inventory.getInAmount() != null ? inventory.getInAmount().toString() : "");
                row.createCell(5).setCellValue(inventory.getOutAmount() != null ? inventory.getOutAmount().toString() : "");
                row.createCell(6).setCellValue(inventory.getBalance() != null ? inventory.getBalance().toString() : "");
                row.createCell(7).setCellValue(inventory.getRemarks());
            }

            for (int i = 0; i < 8; i++) {
                inventorySheet.autoSizeColumn(i);
            }

            // Create a bar chart for this product
            XSSFDrawing drawing = inventorySheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(rowNum + 2);
            anchor.setCol2(10);
            anchor.setRow2(rowNum + 20);

            XSSFChart chart = drawing.createChart(anchor);
            XDDFCategoryAxis categoryAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis valueAxis = chart.createValueAxis(AxisPosition.LEFT);
            XDDFDataSource categories = XDDFDataSourcesFactory.fromStringCellRange(inventorySheet, new CellRangeAddress(1, rowNum - 1, 0, 0));
            XDDFNumericalDataSource values = XDDFDataSourcesFactory.fromNumericCellRange(inventorySheet, new CellRangeAddress(1, rowNum - 1, 3, 3));

            XDDFChartData data = chart.createData(ChartTypes.BAR, categoryAxis, valueAxis);
            XDDFChartData.Series series = data.addSeries(categories, values);
            series.setTitle("Sales Data", null);

            // Set chart title and axes labels
            chart.setTitleText("Sales Data for " + productName);
            chart.setTitleOverlay(false);
            chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

            chart.plot(data);
        }
    }

    private void createSalesSummarySheet(XSSFWorkbook workbook) {
        List<Inventory> allInventories = inventoryRepository.findAll();

        // Create Sales Summary sheet
        XSSFSheet salesSummarySheet = workbook.createSheet("Sales Summary");

        // Create and style the header row for the "Sales Summary" title
        Row titleRow = salesSummarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Sales Summary");

        // Style for the title
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 24); // Larger font size
        titleFont.setColor(IndexedColors.WHITE.getIndex());
        titleStyle.setFont(titleFont);

        // Custom color for title background
        XSSFColor titleColor = new XSSFColor(new byte[] {86, 61, 45}); // RGB: (86, 61, 45)
        titleStyle.setFillForegroundColor(titleColor);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);

        // Merge cells for the title
        salesSummarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5)); // Adjusted range for new column

        // Create header row for the columns
        Row headerRow = salesSummarySheet.createRow(1);
        headerRow.createCell(0).setCellValue("Entry Date");
        headerRow.createCell(1).setCellValue("Product Name");
        headerRow.createCell(2).setCellValue("Description");
        headerRow.createCell(3).setCellValue("Total Out Amount");
        headerRow.createCell(4).setCellValue("Total Quantity");
        headerRow.createCell(5).setCellValue("Total Summary"); // New header for summary

        // Style for the header row
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14); // Larger font size
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        // Custom color for header background
        XSSFColor headerColor = new XSSFColor(new byte[] {(byte) 170, (byte) 130, 115}); // Lighter RGB: (170, 130, 115)
        headerStyle.setFillForegroundColor(headerColor);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerRow.forEach(cell -> cell.setCellStyle(headerStyle));

        // Aggregate sales data
        Map<String, Map<String, SalesData>> salesDataMap = aggregateSalesData(allInventories);

        int rowNum = 2; // Start from row 2 to leave space for title and headers
        for (Map.Entry<String, Map<String, SalesData>> productEntry : salesDataMap.entrySet()) {
            String productName = productEntry.getKey();
            BigDecimal productTotalOutAmount = BigDecimal.ZERO;
            int productTotalQuantity = 0;

            for (Map.Entry<String, SalesData> dateEntry : productEntry.getValue().entrySet()) {
                SalesData salesData = dateEntry.getValue();
                Row row = salesSummarySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dateEntry.getKey()); // Entry Date
                row.createCell(1).setCellValue(productName); // Product Name
                row.createCell(2).setCellValue("Sale of Merchandise"); // Description
                row.createCell(3).setCellValue("₱ " + salesData.getTotalOutAmount().toString()); // Total Out Amount
                row.createCell(4).setCellValue(salesData.getTotalQuantity()); // Total Quantity

                // Accumulate totals
                productTotalOutAmount = productTotalOutAmount.add(salesData.getTotalOutAmount());
                productTotalQuantity += salesData.getTotalQuantity();
            }

            // Add total summary row for the product
            Row summaryRow = salesSummarySheet.createRow(rowNum++);
            summaryRow.createCell(0).setCellValue("Total for " + productName); // Label
            summaryRow.createCell(1).setCellValue(""); // Empty cell
            summaryRow.createCell(2).setCellValue(""); // Empty cell
            summaryRow.createCell(3).setCellValue("₱ " + productTotalOutAmount.toString()); // Total Out Amount
            summaryRow.createCell(4).setCellValue(productTotalQuantity); // Total Quantity
            summaryRow.createCell(5).setCellValue("₱ " + productTotalOutAmount.toString()); // Summary

            rowNum += 2;
        }

        for (int i = 0; i < 6; i++) {
            salesSummarySheet.autoSizeColumn(i);
        }
    }

    private Map<String, Map<String, SalesData>> aggregateSalesData(List<Inventory> inventories) {
        return inventories.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getProductName,
                        Collectors.groupingBy(
                                Inventory::getEntryDate,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            BigDecimal totalOutAmount = list.stream()
                                                    .map(Inventory::getOutAmount)
                                                    .filter(amount -> amount != null)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            int totalQuantity = list.stream()
                                                    .mapToInt(Inventory::getQuantity)
                                                    .sum();
                                            return new SalesData(totalOutAmount, totalQuantity);
                                        }
                                )
                        )
                ));
    }

    private static class SalesData {
        private final BigDecimal totalOutAmount;
        private final int totalQuantity;

        public SalesData(BigDecimal totalOutAmount, int totalQuantity) {
            this.totalOutAmount = totalOutAmount;
            this.totalQuantity = totalQuantity;
        }

        public BigDecimal getTotalOutAmount() {
            return totalOutAmount;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }
    }
}
