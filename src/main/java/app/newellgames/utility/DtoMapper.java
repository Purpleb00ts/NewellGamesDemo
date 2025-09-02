package app.newellgames.utility;

import app.newellgames.review.model.Review;
import app.newellgames.user.model.User;
import app.newellgames.web.dto.*;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {
    public static EditProfileRequest mapUserToUserEditRequest(User user) {

        return EditProfileRequest.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    public static AddReviewRequest mapReviewToAddReviewRequest() { //Pointless mapper, used only for form

        return AddReviewRequest.builder()
                .title("")
                .description("")
                .build();
    }

    public static DepositRequest mapDepositToDepositRequest() { //Pointless mapper, used only for form
        return DepositRequest.builder().build();
    }

    public static LoginRequest mapLoginToLoginRequest() { //Pointless mapper, used only for form
        return LoginRequest.builder().build();
    }

    public static RegisterRequest mapRegisterToRegisterRequest() { //Pointless mapper, used only for form
        return RegisterRequest.builder().build();
    }
}
