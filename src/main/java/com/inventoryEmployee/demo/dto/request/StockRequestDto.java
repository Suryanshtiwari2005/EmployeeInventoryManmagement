package com.inventoryEmployee.demo.dto.request;

public class StockRequestDto {
    private Long productId;
    private Integer quantity;
    private Long employeeId; // We only need the ID, not the whole object
    private String reason;   // Assuming String or Enum for StockMovementReason
    private String notes;

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
