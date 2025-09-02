package app.newellgames.web.dto;

import app.newellgames.review.model.ReviewType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddReviewRequest {
    @NotNull
    @Size(min = 1, max = 20)
    private String title;
    @NotNull
    @Size(min = 1, max = 255)
    private String description;
    @NotNull
    private ReviewType type;
}
