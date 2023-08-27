package form;

import org.springframework.web.multipart.MultipartFile;

public class AudioForm {
    private MultipartFile[] files;

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }
}
