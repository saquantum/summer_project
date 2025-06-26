package org.example.mail.Result;

public class ParsedTokenResult {
    private final String email;
    private final String tokenId;

    public ParsedTokenResult(String email, String tokenId) {
        this.email = email;
        this.tokenId = tokenId;
    }

    public String getEmail() {
        return email;
    }

    public String getTokenId() {
        return tokenId;
    }
}
