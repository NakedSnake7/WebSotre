package com.WeedTitlan.server.dto;

public class CloudinaryUploadResult {
    private String secureUrl;
    private String publicId;

    public CloudinaryUploadResult(String secureUrl, String publicId) {
        this.secureUrl = secureUrl;
        this.publicId = publicId;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }
}

