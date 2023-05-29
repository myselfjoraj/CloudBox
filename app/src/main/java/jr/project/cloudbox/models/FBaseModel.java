package jr.project.cloudbox.models;

import java.io.Serializable;

public class FBaseModel implements Serializable {
    String id,fileName,fileSize,fileUrl,uid,trash;
    boolean isTrsh;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTrash() {
        return trash;
    }

    public void setTrash(String trash) {
        this.trash = trash;
    }

    public boolean isTrsh() {
        return isTrsh;
    }

    public void setTrsh(boolean trash) {
        isTrsh = trash;
    }
}
