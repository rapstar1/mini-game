package com.example.demo.models;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetWalletAddressRequest {
    private String telegramId;
    private String walletAddress;
}
