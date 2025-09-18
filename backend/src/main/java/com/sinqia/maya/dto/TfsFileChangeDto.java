package com.sinqia.maya.dto;

public class TfsFileChangeDto {
    private String fileName;
    private String filePath;
    private String changeType; // ADDED, MODIFIED, DELETED, RENAMED
    private String previousPath; // Para casos de rename
    private String contentBefore; // Conteúdo antes da mudança
    private String contentAfter;  // Conteúdo após a mudança
    private int linesAdded;
    private int linesRemoved;
    private String fileType; // java, xml, properties, etc.
    private long fileSize;
    
    // Construtores
    public TfsFileChangeDto() {}
    
    public TfsFileChangeDto(String fileName, String filePath, String changeType) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.changeType = changeType;
    }
    
    // Getters e Setters
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getChangeType() {
        return changeType;
    }
    
    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
    
    public String getPreviousPath() {
        return previousPath;
    }
    
    public void setPreviousPath(String previousPath) {
        this.previousPath = previousPath;
    }
    
    public String getContentBefore() {
        return contentBefore;
    }
    
    public void setContentBefore(String contentBefore) {
        this.contentBefore = contentBefore;
    }
    
    public String getContentAfter() {
        return contentAfter;
    }
    
    public void setContentAfter(String contentAfter) {
        this.contentAfter = contentAfter;
    }
    
    public int getLinesAdded() {
        return linesAdded;
    }
    
    public void setLinesAdded(int linesAdded) {
        this.linesAdded = linesAdded;
    }
    
    public int getLinesRemoved() {
        return linesRemoved;
    }
    
    public void setLinesRemoved(int linesRemoved) {
        this.linesRemoved = linesRemoved;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    // Método utilitário para determinar se é arquivo Java
    public boolean isJavaFile() {
        return fileName != null && fileName.endsWith(".java");
    }
    
    // Método utilitário para determinar se houve mudanças significativas
    public boolean hasSignificantChanges() {
        return linesAdded > 0 || linesRemoved > 0;
    }
}
