package app.newellgames.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditProfileRequest {
    @Size(min = 4, message = "Username must be at least 4 symbols")
    private String username;

    @Email (message = "Incorrect email")
    private String email;

    private String profilePicture;
}
