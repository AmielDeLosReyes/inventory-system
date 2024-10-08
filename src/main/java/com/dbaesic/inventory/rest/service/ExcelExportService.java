package com.dbaesic.inventory.rest.service;

import com.dbaesic.inventory.rest.model.Inventory;
import com.dbaesic.inventory.rest.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
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
        createPurchaseInventorySummarySheet(workbook);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void createInventorySheet(XSSFWorkbook workbook) {
        List<String> productNames = inventoryRepository.findAllUniqueProductNames();

        for (String productName : productNames) {
            List<Inventory> inventories = inventoryRepository.findByProductName(productName);

            // Create Inventory sheet
            XSSFSheet inventorySheet = workbook.createSheet(productName);

            // Define header cell style
            XSSFColor titleColor = new XSSFColor(new byte[]{86, 61, 45}); // RGB: (86, 61, 45)
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(titleColor);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Define header font style
            XSSFFont headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex()); // Set font color to white
            headerFont.setBold(true); // Make font bold if needed
            headerStyle.setFont(headerFont);

            // Create header row and apply styles
            Row headerRow = inventorySheet.createRow(0);
            createStyledHeaderCell(headerRow, 0, "Date", headerStyle);
            createStyledHeaderCell(headerRow, 1, "Description", headerStyle);
            createStyledHeaderCell(headerRow, 2, "Cost", headerStyle);
            createStyledHeaderCell(headerRow, 3, "Quantity", headerStyle);
            createStyledHeaderCell(headerRow, 4, "In Amount", headerStyle);
            createStyledHeaderCell(headerRow, 5, "Out Amount", headerStyle);
            createStyledHeaderCell(headerRow, 6, "Balance", headerStyle);
            createStyledHeaderCell(headerRow, 7, "Remarks", headerStyle);

            int rowNum = 1;

            // Calculate totals for Purchase of Inventory, Sale of Merchandise, Damaged Goods, and Initial Inventory
            int purchaseQuantity = 0;
            int saleQuantity = 0;
            int damagedQuantity = 0;
            int initialInventoryQuantity = 0;
            BigDecimal purchaseTotal = BigDecimal.ZERO;
            BigDecimal saleTotal = BigDecimal.ZERO;
            BigDecimal damagedTotal = BigDecimal.ZERO;
            BigDecimal initialInventoryTotal = BigDecimal.ZERO;

            // Define cell styles for different types
            CellStyle purchaseStyle = workbook.createCellStyle();
            purchaseStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)0, (byte)128, (byte)0})); // Matte green color
            purchaseStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle saleStyle = workbook.createCellStyle();
            saleStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)178, (byte)34, (byte)34})); // Matte red color
            saleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle damagedStyle = workbook.createCellStyle();
            damagedStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)255, (byte)165, (byte)0})); // Orange color
            damagedStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle initialInventoryStyle = workbook.createCellStyle();
            initialInventoryStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte)70, (byte)130, (byte)180})); // Steel blue color
            initialInventoryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Inventory inventory : inventories) {
                Row row = inventorySheet.createRow(rowNum++);
                LocalDate entryDate = null;
                try {
                    entryDate = LocalDate.parse(inventory.getEntryDate());
                } catch (DateTimeParseException e) {
                    log.error("Failed to parse date: {}", inventory.getEntryDate(), e);
                }
                row.createCell(0).setCellValue(entryDate != null ? entryDate.toString() : "");
                row.createCell(1).setCellValue(inventory.getDescription());
                row.createCell(2).setCellValue(inventory.getCost() != null ? inventory.getCost().toString() : "");
                row.createCell(3).setCellValue(inventory.getQuantity());
                row.createCell(4).setCellValue(inventory.getInAmount() != null ? inventory.getInAmount().toString() : "");
                row.createCell(5).setCellValue(inventory.getOutAmount() != null ? inventory.getOutAmount().toString() : "");
                row.createCell(6).setCellValue(inventory.getBalance() != null ? inventory.getBalance().toString() : "");
                row.createCell(7).setCellValue(inventory.getRemarks());

                // Apply color based on description and update totals
                if ("Purchase of Inventory".equals(inventory.getDescription())) {
                    purchaseTotal = purchaseTotal.add(inventory.getInAmount() != null ? inventory.getInAmount() : BigDecimal.ZERO);
                    purchaseQuantity += inventory.getQuantity();
                    row.setRowStyle(purchaseStyle);
                } else if ("Sale of Merchandise".equals(inventory.getDescription())) {
                    saleTotal = saleTotal.add(inventory.getOutAmount() != null ? inventory.getOutAmount() : BigDecimal.ZERO);
                    saleQuantity += inventory.getQuantity();
                    row.setRowStyle(saleStyle);
                } else if ("Damaged Goods".equals(inventory.getDescription())) {
                    damagedTotal = damagedTotal.add(inventory.getOutAmount() != null ? inventory.getOutAmount() : BigDecimal.ZERO);
                    damagedQuantity += inventory.getQuantity();
                    row.setRowStyle(damagedStyle);
                    // Add to Sale of Merchandise total
                    saleTotal = saleTotal.add(inventory.getOutAmount() != null ? inventory.getOutAmount() : BigDecimal.ZERO);
                    saleQuantity += inventory.getQuantity();
                } else if ("Initial Inventory".equals(inventory.getDescription())) {
                    initialInventoryTotal = initialInventoryTotal.add(inventory.getInAmount() != null ? inventory.getInAmount() : BigDecimal.ZERO);
                    initialInventoryQuantity += inventory.getQuantity();
                    // Add to Purchase of Inventory total
                    purchaseTotal = purchaseTotal.add(inventory.getInAmount() != null ? inventory.getInAmount() : BigDecimal.ZERO);
                    purchaseQuantity += inventory.getQuantity();
                    row.setRowStyle(initialInventoryStyle);
                }
            }

            // Highlight totals
            Row purchaseRow = inventorySheet.createRow(rowNum++);
            Cell purchaseLabelCell = purchaseRow.createCell(0);
            purchaseLabelCell.setCellValue("Purchase of Inventory");
            applyCellColor(purchaseLabelCell, new XSSFColor(new byte[]{(byte)0, (byte)128, (byte)0}));  // Matte green color
            purchaseRow.createCell(1).setCellValue("₱ " + purchaseTotal.toString());
            purchaseRow.createCell(3).setCellValue(purchaseQuantity); // Total quantity purchased

            Row saleRow = inventorySheet.createRow(rowNum++);
            Cell saleLabelCell = saleRow.createCell(0);
            saleLabelCell.setCellValue("Sale of Merchandise");
            applyCellColor(saleLabelCell, new XSSFColor(new byte[]{(byte)178, (byte)34, (byte)34}));  // Matte red color
            saleRow.createCell(1).setCellValue("₱ " + saleTotal.toString());
            saleRow.createCell(3).setCellValue(saleQuantity); // Total quantity sold

            Row damagedRow = inventorySheet.createRow(rowNum++);
            Cell damagedLabelCell = damagedRow.createCell(0);
            damagedLabelCell.setCellValue("Damaged Goods");
            applyCellColor(damagedLabelCell, new XSSFColor(new byte[]{(byte)255, (byte)165, (byte)0}));  // Orange color for Damaged Goods
            damagedRow.createCell(1).setCellValue("₱ " + damagedTotal.toString());
            damagedRow.createCell(3).setCellValue(damagedQuantity); // Total quantity damaged

            // Highlight Initial Inventory totals
            Row initialInventoryRow = inventorySheet.createRow(rowNum++);
            Cell initialInventoryLabelCell = initialInventoryRow.createCell(0);
            initialInventoryLabelCell.setCellValue("Initial Inventory");
            applyCellColor(initialInventoryLabelCell, new XSSFColor(new byte[]{(byte)70, (byte)130, (byte)180}));  // Steel blue color
            initialInventoryRow.createCell(1).setCellValue("₱ " + initialInventoryTotal.toString());
            initialInventoryRow.createCell(3).setCellValue(initialInventoryQuantity); // Total quantity initial inventory

            // Add stock status below the totals
            Row stockStatusRow = inventorySheet.createRow(rowNum++);
            Cell stockStatusLabelCell = stockStatusRow.createCell(0);
            stockStatusLabelCell.setCellValue("Stock Status");

            int stockQuantity = purchaseQuantity - saleQuantity - damagedQuantity + initialInventoryQuantity;  // Include initial inventory
            String stockStatus = getStockStatus(BigDecimal.valueOf(stockQuantity));
            stockStatusRow.createCell(3).setCellValue(stockQuantity); // Show stock quantity
            stockStatusRow.createCell(1).setCellValue(stockStatus); // Show stock status
            applyStockStatusColor(stockStatusRow.getCell(1), stockStatus); // Color based on stock status

            // Adjust column widths
            for (int i = 0; i < 8; i++) {
                inventorySheet.autoSizeColumn(i);
            }
        }
    }


    private void createStyledHeaderCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String getStockStatus(BigDecimal balance) {
        if (balance == null) return "Unknown";
        if (balance.compareTo(BigDecimal.ZERO) == 0) return "Out of Stock";
        if (balance.compareTo(BigDecimal.valueOf(50)) <= 0) return "Low on Stock";
        return "In Stock";
    }

    private void applyStockStatusColor(Cell cell, String status) {
        CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
        switch (status) {
            case "In Stock":
                style.setFillForegroundColor(new XSSFColor(new byte[] {(byte)144, (byte)238, (byte)144})); // Light green color
                break;
            case "Low on Stock":
                style.setFillForegroundColor(new XSSFColor(new byte[] {(byte)255, (byte)255, (byte)0})); // Yellow color
                break;
            case "Out of Stock":
                style.setFillForegroundColor(new XSSFColor(new byte[] {(byte)255, (byte)140, (byte)0})); // Dark orange color
                break;
            default:
                style.setFillForegroundColor(new XSSFColor(new byte[] {(byte)255, (byte)255, (byte)255})); // Default white color
                break;
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }

    private void applyCellColor(Cell cell, XSSFColor color) {
        CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cell.setCellStyle(style);
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

        // Filter and aggregate sales data
        Map<String, Map<String, SalesData>> salesDataMap = aggregateSalesData(allInventories);

        int rowNum = 2; // Start from row 2 to leave space for title and headers
        for (Map.Entry<String, Map<String, SalesData>> productEntry : salesDataMap.entrySet()) {
            String productName = productEntry.getKey();
            BigDecimal productTotalOutAmount = BigDecimal.ZERO;
            int productTotalQuantity = 0;

            // Sort the dates before adding to the sheet
            List<Map.Entry<String, SalesData>> sortedEntries = new ArrayList<>(productEntry.getValue().entrySet());
            sortedEntries.sort(Map.Entry.comparingByKey());

            for (Map.Entry<String, SalesData> dateEntry : sortedEntries) {
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

            // Create a distinct style for the summary row
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true); // Bold font
            summaryFont.setColor(IndexedColors.WHITE.getIndex()); // White font color
            summaryStyle.setFont(summaryFont);

            // Custom color for summary row background
            XSSFColor summaryColor = new XSSFColor(new byte[] {86, 61, 45});
            summaryStyle.setFillForegroundColor(summaryColor);
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Set border style for the summary row
            summaryStyle.setBorderBottom(BorderStyle.THICK);
            summaryStyle.setBorderTop(BorderStyle.THICK);
            summaryStyle.setBorderLeft(BorderStyle.THICK);
            summaryStyle.setBorderRight(BorderStyle.THICK);
            summaryStyle.setAlignment(HorizontalAlignment.CENTER); // Center alignment
            summaryStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Apply the style to each cell in the summary row
            for (int i = 0; i < 6; i++) {
                summaryRow.getCell(i).setCellStyle(summaryStyle);
            }

            // Add an extra blank row below the summary row for spacing
            rowNum += 2;
        }

        for (int i = 0; i < 6; i++) {
            salesSummarySheet.autoSizeColumn(i);
        }
    }


    private Map<String, Map<String, SalesData>> aggregateSalesData(List<Inventory> inventories) {
        return inventories.stream()
                .filter(inventory -> inventory.getOutAmount() != null && inventory.getOutAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(
                        Inventory::getProductName,
                        Collectors.groupingBy(
                                inventory -> inventory.getEntryDate().toString(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            BigDecimal totalOutAmount = list.stream()
                                                    .map(Inventory::getOutAmount)
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
    private void createPurchaseInventorySummarySheet(XSSFWorkbook workbook) {
        List<Inventory> allInventories = inventoryRepository.findAll();

        // Create Purchase Inventory Summary sheet
        XSSFSheet purchaseSummarySheet = workbook.createSheet("Purchase Inventory Summary");

        // Create and style the header row for the "Purchase Inventory Summary" title
        Row titleRow = purchaseSummarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Purchase Inventory Summary");

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
        purchaseSummarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5)); // Adjusted range for purchase inventory

        // Create header row for the columns
        Row headerRow = purchaseSummarySheet.createRow(1);
        headerRow.createCell(0).setCellValue("Entry Date");
        headerRow.createCell(1).setCellValue("Product Name");
        headerRow.createCell(2).setCellValue("Description");
        headerRow.createCell(3).setCellValue("Total In Amount");
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

        // Filter and aggregate purchase data
        Map<String, Map<String, PurchaseData>> purchaseDataMap = aggregatePurchaseData(allInventories);

        int rowNum = 2; // Start from row 2 to leave space for title and headers
        for (Map.Entry<String, Map<String, PurchaseData>> productEntry : purchaseDataMap.entrySet()) {
            String productName = productEntry.getKey();
            BigDecimal productTotalInAmount = BigDecimal.ZERO;
            int productTotalQuantity = 0;

            // Sort entries by date
            Map<String, PurchaseData> sortedDateEntries = new TreeMap<>(productEntry.getValue());

            for (Map.Entry<String, PurchaseData> dateEntry : sortedDateEntries.entrySet()) {
                PurchaseData purchaseData = dateEntry.getValue();
                Row row = purchaseSummarySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dateEntry.getKey()); // Entry Date
                row.createCell(1).setCellValue(productName); // Product Name
                row.createCell(2).setCellValue("Purchase of Inventory"); // Description
                row.createCell(3).setCellValue("₱ " + purchaseData.getTotalInAmount().toString()); // Total In Amount
                row.createCell(4).setCellValue(purchaseData.getTotalQuantity()); // Total Quantity

                // Accumulate totals
                productTotalInAmount = productTotalInAmount.add(purchaseData.getTotalInAmount());
                productTotalQuantity += purchaseData.getTotalQuantity();
            }

            // Add total summary row for the product
            Row summaryRow = purchaseSummarySheet.createRow(rowNum++);
            summaryRow.createCell(0).setCellValue("Total for " + productName); // Label
            summaryRow.createCell(1).setCellValue(""); // Empty cell
            summaryRow.createCell(2).setCellValue(""); // Empty cell
            summaryRow.createCell(3).setCellValue("₱ " + productTotalInAmount.toString()); // Total In Amount
            summaryRow.createCell(4).setCellValue(productTotalQuantity); // Total Quantity
            summaryRow.createCell(5).setCellValue("₱ " + productTotalInAmount.toString()); // Summary

            // Apply the same summary style
            CellStyle summaryStyle = workbook.createCellStyle();
            Font summaryFont = workbook.createFont();
            summaryFont.setBold(true); // Bold font
            summaryFont.setColor(IndexedColors.WHITE.getIndex()); // White font color
            summaryStyle.setFont(summaryFont);

            // Custom color for summary row background
            XSSFColor summaryColor = new XSSFColor(new byte[] {86, 61, 45});
            summaryStyle.setFillForegroundColor(summaryColor);
            summaryStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Set border style for the summary row
            summaryStyle.setBorderBottom(BorderStyle.THICK);
            summaryStyle.setBorderTop(BorderStyle.THICK);
            summaryStyle.setBorderLeft(BorderStyle.THICK);
            summaryStyle.setBorderRight(BorderStyle.THICK);
            summaryStyle.setAlignment(HorizontalAlignment.CENTER); // Center alignment
            summaryStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Apply the style to each cell in the summary row
            for (int i = 0; i < 6; i++) {
                summaryRow.getCell(i).setCellStyle(summaryStyle);
            }

            // Add an extra blank row below the summary row for spacing
            rowNum += 2;
        }

        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            purchaseSummarySheet.autoSizeColumn(i);
        }
    }

    private Map<String, Map<String, PurchaseData>> aggregatePurchaseData(List<Inventory> inventories) {
        return inventories.stream()
                .filter(inventory -> "Purchase of Inventory".equals(inventory.getDescription())) // Filter purchases only
                .collect(Collectors.groupingBy(
                        Inventory::getProductName,
                        Collectors.groupingBy(
                                Inventory::getEntryDate,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            BigDecimal totalInAmount = list.stream()
                                                    .map(Inventory::getInAmount)
                                                    .filter(Objects::nonNull)
                                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                                            int totalQuantity = list.stream().mapToInt(Inventory::getQuantity).sum();
                                            return new PurchaseData(totalInAmount, totalQuantity);
                                        }
                                )
                        )
                ));
    }

    // PurchaseData class to hold aggregated purchase data
    public static class PurchaseData {
        private BigDecimal totalInAmount;
        private int totalQuantity;

        public PurchaseData(BigDecimal totalInAmount, int totalQuantity) {
            this.totalInAmount = totalInAmount;
            this.totalQuantity = totalQuantity;
        }

        public BigDecimal getTotalInAmount() {
            return totalInAmount;
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }
    }



}
