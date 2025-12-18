package com.video.app.service.mapper;

import com.video.app.domain.User;
import com.video.app.domain.VideoCredito;
import com.video.app.service.dto.UserDTO;
import com.video.app.service.dto.VideoCreditoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VideoCredito} and its DTO {@link VideoCreditoDTO}.
 */
@Mapper(componentModel = "spring")
public interface VideoCreditoMapper extends EntityMapper<VideoCreditoDTO, VideoCredito> {
    @Mapping(target = "user", source = "user", qualifiedByName = "userLogin")
    VideoCreditoDTO toDto(VideoCredito s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
