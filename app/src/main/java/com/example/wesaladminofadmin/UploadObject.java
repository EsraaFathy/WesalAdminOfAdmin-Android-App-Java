package com.example.wesaladminofadmin;

public class UploadObject {
    private String imageName;
    private String imageUri;

    public UploadObject() {
    }

    public UploadObject(String imageName, String imageUri) {
        if (imageName.trim().equals("")){
            imageName="No name";
        }
        this.imageName = imageName;
        this.imageUri = imageUri;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
