package recommender.persistence.manager.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "updateBuilder")
public class UpdateDTO {
    String username;
    Integer uid;
}
