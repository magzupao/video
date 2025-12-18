package com.video.app.service.mapper;

import com.video.app.domain.User;
import com.video.app.domain.Video;
import com.video.app.service.dto.UserDTO;
import com.video.app.service.dto.VideoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Video} and its DTO {@link VideoDTO}.
 */
@Mapper(componentModel = "spring")
public interface VideoMapper extends EntityMapper<VideoDTO, Video> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    VideoDTO toDto(Video s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
