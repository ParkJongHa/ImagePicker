package picker.imagepicker;

class APickerItem {

    final String filePath;
    final String fileFolder;
    final String fileId;
    final int position;

    private String thumbnailPath;
    private boolean isSelected;

    APickerItem(String _filePath, String _fileFolder, String _fileId, int _position) {
        this.filePath = _filePath;
        this.fileFolder = _fileFolder;
        this.fileId = _fileId;
        this.position = _position;
    }

    boolean isSelected() {
        return isSelected;
    }

    void setSelected(boolean selected) {
        isSelected = selected;
    }

    String getThumbnailPath() {
        return thumbnailPath;
    }

    void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

}
