package jr.project.cloudbox.models;

public class FolderModel {

    String folderName;
    long folderSize;
    int fileCount;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public long getFolderSize() {
        return folderSize;
    }

    public void setFolderSize(long folderSize) {
        this.folderSize = folderSize;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
}
