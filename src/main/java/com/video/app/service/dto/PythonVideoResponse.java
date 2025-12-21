package com.video.app.service.dto;

import java.util.List;

// DTO para la respuesta de Python
public class PythonVideoResponse {

    private String status;
    private String video_path;
    private VideoMetadata metadata;

    public PythonVideoResponse() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVideo_path() {
        return video_path;
    }

    public void setVideo_path(String video_path) {
        this.video_path = video_path;
    }

    public VideoMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(VideoMetadata metadata) {
        this.metadata = metadata;
    }

    public static class VideoMetadata {

        private String filename;
        private String full_path;
        private String created_at;
        private Integer images_used;
        private Double duration;
        private String audio_file;
        private String resolution;
        private Integer fps;
        private Double file_size_mb;
        private List<String> image_order;
        private Double duration_per_image;

        public VideoMetadata() {}

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getFull_path() {
            return full_path;
        }

        public void setFull_path(String full_path) {
            this.full_path = full_path;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public Integer getImages_used() {
            return images_used;
        }

        public void setImages_used(Integer images_used) {
            this.images_used = images_used;
        }

        public Double getDuration() {
            return duration;
        }

        public void setDuration(Double duration) {
            this.duration = duration;
        }

        public String getAudio_file() {
            return audio_file;
        }

        public void setAudio_file(String audio_file) {
            this.audio_file = audio_file;
        }

        public String getResolution() {
            return resolution;
        }

        public void setResolution(String resolution) {
            this.resolution = resolution;
        }

        public Integer getFps() {
            return fps;
        }

        public void setFps(Integer fps) {
            this.fps = fps;
        }

        public Double getFile_size_mb() {
            return file_size_mb;
        }

        public void setFile_size_mb(Double file_size_mb) {
            this.file_size_mb = file_size_mb;
        }

        public List<String> getImage_order() {
            return image_order;
        }

        public void setImage_order(List<String> image_order) {
            this.image_order = image_order;
        }

        public Double getDuration_per_image() {
            return duration_per_image;
        }

        public void setDuration_per_image(Double duration_per_image) {
            this.duration_per_image = duration_per_image;
        }
    }
}
