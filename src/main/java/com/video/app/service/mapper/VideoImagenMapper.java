package com.video.app.service.mapper;

import com.video.app.domain.Video;
import com.video.app.domain.VideoImagen;
import com.video.app.service.dto.VideoDTO;
import com.video.app.service.dto.VideoImagenDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link VideoImagen} and its DTO {@link VideoImagenDTO}.
 */
@Mapper(componentModel = "spring")
public interface VideoImagenMapper extends EntityMapper<VideoImagenDTO, VideoImagen> {
    @Mapping(target = "video", source = "video", qualifiedByName = "videoId")
    VideoImagenDTO toDto(VideoImagen s);

    @Named("videoId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    VideoDTO toDtoVideoId(Video video);
}
