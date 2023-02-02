package com.example.majorproject;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {


    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;


    @Autowired
    WalletRepository walletRepository;

    @KafkaListener(topics = "create_wallet",groupId = "test1234")
    public void createWallet(String message){

        Wallet wallet = Wallet.builder().userName(message).balance(100).build();

        walletRepository.save(wallet);

    }

    @KafkaListener(topics = "update_wallet",groupId = "test1234")
    public void updateWallet(String message) throws JsonProcessingException {

        //decoded back to JSonObject and extract information
        JSONObject jsonObject = objectMapper.convertValue(message,JSONObject.class);

        String fromUser = (String)jsonObject.get("fromUser");
        String toUser = (String)jsonObject.get("toUser");
        int transactionAmount = (Integer)jsonObject.get("amount");
        String transactionId = (String)jsonObject.get("transactionId");


        JSONObject returnObject = new JSONObject();

        returnObject.put("transactionId",transactionId);

        Wallet fromUserWallet = walletRepository.getWalletByUserName(fromUser);

        Wallet receiverWallet = walletRepository.getWalletByUserName(toUser);

        if(fromUserWallet.getBalance()>=transactionAmount){

            //That is a succesfull transaction
            returnObject.put("status","SUCESS");

            kafkaTemplate.send("update_transaction",objectMapper.writeValueAsString(returnObject));


            //Update the sender and receiver's wallet
            //Write it as h.w


        }else{

            returnObject.put("status","FAILED");
            kafkaTemplate.send("update_transaction",objectMapper.writeValueAsString(returnObject));

            //Not update the wallets.
        }


    }

}
