package com.example.demo.models;
import lombok.Getter;
import lombok.Setter;
/**
 * Set Wallet Address Request
 *
 * @author Sunny SHEN
 */
@Getter
@Setter
public class SetWalletAddressRequest {
    private String telegramId;
    private String walletAddress;
}
