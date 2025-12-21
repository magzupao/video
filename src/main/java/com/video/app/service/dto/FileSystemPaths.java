package com.video.app.service.dto;

// Helper para paths del filesystem
public class FileSystemPaths {

    private String imagesPath;
    private String audioPath;
    private String videoOutputPath;

    public FileSystemPaths(String imagesPath, String audioPath, String videoOutputPath) {
        this.imagesPath = imagesPath;
        this.audioPath = audioPath;
        this.videoOutputPath = videoOutputPath;
    }

    public String getImagesPath() {
        return imagesPath;
    }

    public void setImagesPath(String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }

    public String getVideoOutputPath() {
        return videoOutputPath;
    }

    public void setVideoOutputPath(String videoOutputPath) {
        this.videoOutputPath = videoOutputPath;
    }
}
