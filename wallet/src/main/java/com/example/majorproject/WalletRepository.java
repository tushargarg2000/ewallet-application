package com.example.majorproject;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet,Integer> {


    Wallet getWalletByUserName(String userName);
}
