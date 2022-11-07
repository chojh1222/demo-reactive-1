package com.example.demo.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
public class File {
	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Column(name = "CONTENT_LENGTH")
    private Long contentLength;

    @Column(name = "CONTENT_OFFSET")
    private Long contentOffset;

    @Column(name = "LAST_UPLOADED_CHUNK_NUMBER")
    private Long lastUploadedChunkNumber;

    @Column(name = "ORIGINAL_NAME")
    private String originalName;

    @Column(name = "FINGERPRINT")
    private String fingerprint;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
    
    


    public File() {
		super();
		// TODO Auto-generated constructor stub
	}
    
    

	public File(Long id, String mimeType, Long contentLength, Long contentOffset, Long lastUploadedChunkNumber,
			String originalName, String fingerprint, LocalDateTime createdAt, LocalDateTime updatedAt) {
		super();
		this.id = id;
		this.mimeType = mimeType;
		this.contentLength = contentLength;
		this.contentOffset = contentOffset;
		this.lastUploadedChunkNumber = lastUploadedChunkNumber;
		this.originalName = originalName;
		this.fingerprint = fingerprint;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public String getMimeType() {
		return mimeType;
	}



	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}



	public Long getContentLength() {
		return contentLength;
	}



	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}



	public Long getContentOffset() {
		return contentOffset;
	}



	public void setContentOffset(Long contentOffset) {
		this.contentOffset = contentOffset;
	}



	public Long getLastUploadedChunkNumber() {
		return lastUploadedChunkNumber;
	}



	public void setLastUploadedChunkNumber(Long lastUploadedChunkNumber) {
		this.lastUploadedChunkNumber = lastUploadedChunkNumber;
	}



	public String getOriginalName() {
		return originalName;
	}



	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}



	public String getFingerprint() {
		return fingerprint;
	}



	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}



	public LocalDateTime getCreatedAt() {
		return createdAt;
	}



	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}



	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}



	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}



	@Override
	public String toString() {
		return "File [id=" + id + ", mimeType=" + mimeType + ", contentLength=" + contentLength + ", contentOffset="
				+ contentOffset + ", lastUploadedChunkNumber=" + lastUploadedChunkNumber + ", originalName="
				+ originalName + ", fingerprint=" + fingerprint + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}



	@PrePersist
    protected void onPersist(){
        createdAt = LocalDateTime.now(ZoneId.systemDefault());
        updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
    
    
}
