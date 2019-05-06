package recommender.persistence.manager.mapper;

import recommender.persistence.entity.ClassProperties;
import recommender.persistence.manager.dto.class_properties.CreateDTO;
import recommender.persistence.manager.dto.class_properties.GetDTO;

public final class ClassPropertiesMapper {
    public static ClassProperties fromDTO(CreateDTO createDTO) {
        return ClassProperties.builder()
                .uri(createDTO.getUri())
                .preference(createDTO.getPreference())
                .confidence(createDTO.getConfidence())
                .activation(createDTO.getActivation())
                .build();
    }

    public static GetDTO toDTO(ClassProperties classProperties) {
        return GetDTO.builder()
                .pid(classProperties.getPid())
                .uri(classProperties.getUri())
                .uid(classProperties.getUser().getUid())
                .preference(classProperties.getPreference())
                .confidence(classProperties.getConfidence())
                .activation(classProperties.getActivation())
                .build();
    }
}
