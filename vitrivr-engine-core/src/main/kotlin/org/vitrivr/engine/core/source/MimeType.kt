package org.vitrivr.engine.core.source

import java.io.File

enum class MimeType(val fileExtension: String, val mimeType: String, val mediaType: MediaType) {

    //Video Mime Types
    AVI("avi", "video/avi", MediaType.VIDEO),
    M4V("m4v", "video/mp4", MediaType.VIDEO),
    M1V("m1v", "video/mpeg", MediaType.VIDEO),
    M2V("m2v", "video/mpeg", MediaType.VIDEO),
    M2P("mp2", "video/mpeg", MediaType.VIDEO),
    MPG("mpg", "video/mpeg", MediaType.VIDEO),
    MPEG("mpeg", "video/mpeg", MediaType.VIDEO),
    MPE("mpe", "video/mpeg", MediaType.VIDEO),
    MPA("mpa", "video/mpeg", MediaType.VIDEO),
    MOV("mov", "video/quicktime", MediaType.VIDEO),
    MOOV("moov", "video/quicktime", MediaType.VIDEO),
    MOVIE("movie", "video/quicktime", MediaType.VIDEO),
    OGV("ogv", "video/ogg", MediaType.VIDEO),
    WEBM("webm", "video/webm", MediaType.VIDEO),
    MP4("mp4", "video/mp4", MediaType.VIDEO),

    //Image Mime Types
    GIF("gif", "image/gif", MediaType.IMAGE),
    BMP("bmp", "image/bmp", MediaType.IMAGE),
    JPG("jpg", "image/jpeg", MediaType.IMAGE),
    JPEG("jpeg", "image/jpeg", MediaType.IMAGE),
    JPE("jpe", "image/jpeg", MediaType.IMAGE),
    JP2("jp2", "image/jp2", MediaType.IMAGE),
    PNG("png", "image/png", MediaType.IMAGE),
    TIF("tif", "image/tiff", MediaType.IMAGE),
    TIFF("tiff", "image/tiff", MediaType.IMAGE),

    //Audio Mime Types
    M4A("m4a", "audio/mp4", MediaType.AUDIO),
    AAC("aac", "audio/aac", MediaType.AUDIO),
    AIF("aif", "audio/aiff", MediaType.AUDIO),
    AIFF("aiff", "audio/aiff", MediaType.AUDIO),
    WAV("wav", "audio/wav", MediaType.AUDIO),
    WAVE("wave", "audio/wav", MediaType.AUDIO),
    MP1("mp1", "audio/mpeg", MediaType.AUDIO),
    MP2("mp3", "audio/mpeg", MediaType.AUDIO),
    OGA("oga", "audio/ogg", MediaType.AUDIO),
    OGG("ogg", "audio/ogg", MediaType.AUDIO),
    FLAC("flac", "audio/flac", MediaType.AUDIO),

    //3D Mime types (self-defimed)
    STL("stl", "application/3d-stl", MediaType.MESH),
    OBJ("obj", "application/3d-obj", MediaType.MESH),
    OFF("off", "application/3d-off", MediaType.MESH)

    ;

    companion object {
        fun getMimeType(fileName: String): MimeType = MimeType.valueOf(fileName.substringAfterLast('.').uppercase())
        fun getMimeType(file: File): MimeType? = MimeType.valueOf(file.extension.uppercase())
    }
}