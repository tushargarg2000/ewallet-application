package com.example.majorproject;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    public void createTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {

        //First of all we will create a transaction Entity and put its status to pending

        Transaction transaction = Transaction.builder().fromUser(transactionRequest.getFromUser())
                        .toUser(transactionRequest.getToUser()).transactionId(UUID.randomUUID().toString())
                        .transactionDate(new Date()).transactionStatus(TransactionStatus.PENDING)
                .amount(transactionRequest.getAmount()).purpose(transactionRequest.getPurpose()).build();


        transactionRepository.save(transaction);


        //Create that JsonObject

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fromUser",transactionRequest.getFromUser());
        jsonObject.put("toUser",transactionRequest.getToUser());
        jsonObject.put("amount",transactionRequest.getAmount());
        jsonObject.put("transactionId",transaction.getTransactionId());

        //Converted to string and send it via kafka to the wallet microservice
        String kafkaMessage = objectMapper.writeValueAsString(jsonObject);
        kafkaTemplate.send("update_wallet",kafkaMessage);



    }

}
