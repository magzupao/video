package com.video.app.service.dto;

// DTO para request CON audio
public class VideoGenerationRequest {

    private String images_path;
    private String audio_path;
    private String video_path;
    private String format;

    public VideoGenerationRequest() {}

    public String getImages_path() {
        return images_path;
    }

    public void setImages_path(String images_path) {
        this.images_path = images_path;
    }

    public String getAudio_path() {
        return audio_path;
    }

    public void setAudio_path(String audio_path) {
        this.audio_path = audio_path;
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
}
