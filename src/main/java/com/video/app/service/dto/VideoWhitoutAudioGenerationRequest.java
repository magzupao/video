package com.video.app.service.dto;

// DTO para request SIN audio
public class VideoWhitoutAudioGenerationRequest {

    private String images_path;
    private String video_path;
    private String format;
    private Integer transicion_segundos;

    public VideoWhitoutAudioGenerationRequest() {}

    public String getImages_path() {
        return images_path;
    }

    public void setImages_path(String images_path) {
        this.images_path = images_path;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getTransicion_segundos() {
        return transicion_segundos;
    }

    public void setTransicion_segundos(Integer transicion_segundos) {
        this.transicion_segundos = transicion_segundos;
    }
}
