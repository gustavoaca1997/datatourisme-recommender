package recommender.persistence.manager.mapper;

import recommender.persistence.entity.User;
import recommender.persistence.manager.dto.user.CreateDTO;
import recommender.persistence.manager.dto.user.GetDTO;
import recommender.persistence.manager.dto.user.UpdateDTO;

public final class UserMapper {
    public static GetDTO toDTO(User user) {
        return GetDTO.builder()
                .username(user.getUsername())
                .uid(user.getUid())
                .build();
    }

    public static User fromDTO(CreateDTO createDTO) {
        return User.builder()
                .username(createDTO.getUsername())
                .build();
    }

    public static void fromDTO(User user, UpdateDTO createDTO) {
        user.setUsername(createDTO.getUsername());
    }
}
