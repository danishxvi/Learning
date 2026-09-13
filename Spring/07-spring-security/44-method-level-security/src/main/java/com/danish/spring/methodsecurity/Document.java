package com.danish.spring.methodsecurity;

public class Document {
    private final Long id;
    private final String owner;
    private final String content;

    public Document(Long id, String owner, String content) {
        this.id = id;
        this.owner = owner;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getOwner() { return owner; }
    public String getContent() { return content; }

    @Override
    public String toString() {
        return "Document{id=" + id + ", owner=" + owner + ", content=" + content + "}";
    }
}
