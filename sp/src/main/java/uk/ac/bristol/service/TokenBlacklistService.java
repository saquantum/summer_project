package uk.ac.bristol.service;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);
}
