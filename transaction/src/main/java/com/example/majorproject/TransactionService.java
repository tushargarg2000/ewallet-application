package com.example.majorproject;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
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

    @Autowired
    RestTemplate restTemplate;

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

    @KafkaListener(topics = {"update_transaction"},groupId = "friends_group")
    public void updateTransaction(String message) throws JsonProcessingException {


        //Decode the message
        JSONObject transactionRequest = objectMapper.readValue(message,JSONObject.class);

        String transactionStatus = (String) transactionRequest.get("status");


        String transactionId = (String) transactionRequest.get("transactionId");

        System.out.println("Reading the transacitonTable Entries"+transactionStatus+"---"+transactionId);

        Transaction t = transactionRepository.findByTransactionId(transactionId);

        t.setTransactionStatus(TransactionStatus.valueOf(transactionStatus));

        transactionRepository.save(t);

        // CALL NOTIFICATION SERVICE AND SEND EMAILS
        callNotificationService(t);

    }

    public void callNotificationService(Transaction transaction){



        String fromUserName  = transaction.getFromUser();
        String toUserName = transaction.getToUser();


        //We need to create that REST API and call User-service

        URI url = URI.create("http://localhost:9999/user/findEmailDto/"+fromUserName);

        HttpEntity httpEntity = new HttpEntity(new HttpHeaders());


        ResponseEntity<JSONObject> fromUserJsonObject = restTemplate.exchange(url, HttpMethod.GET,httpEntity,JSONObject.class);



        URI url1 = URI.create("http://localhost:9999/user/findEmailDto/"+toUserName);
        ResponseEntity<JSONObject> toUserObject = restTemplate.exchange(url1, HttpMethod.GET,httpEntity,JSONObject.class);









    }

}
