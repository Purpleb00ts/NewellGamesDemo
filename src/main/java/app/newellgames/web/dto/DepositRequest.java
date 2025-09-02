package app.newellgames.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "5.00", inclusive = true, message = "Amount must be at least 5.00")
    @DecimalMax(value = "2500.00", inclusive = true, message = "Amount must be at most 2500.00")
    @Digits(integer = 6, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;
}
