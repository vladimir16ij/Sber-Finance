package ru.sbrf.sber.finance.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sbrf.sber.finance.model.*;
import ru.sbrf.sber.finance.model.dtowrapper.AccountTransferStartMessage;
import ru.sbrf.sber.finance.model.dtowrapper.FinishMessage;
import ru.sbrf.sber.finance.model.dtowrapper.Status;
import ru.sbrf.sber.finance.model.dtowrapper.TransferResultMessage;
import ru.sbrf.sber.finance.repositiries.TransactionRepository;

import java.math.BigDecimal;

@Component
public class TransactionService {

    @Autowired
    private KafkaTemplate<String, TransferResultMessage> kafkaTemplate;

    @Autowired
    private TransactionRepository transactionRepository;
    @Transactional(rollbackFor = Exception.class)
    public void doTransferBegin(AccountTransferStartMessage message) {
        //для любых исключений откатываем
        //account period заглушка
        Transaction transactionFrom = createTransaction(message.getAccountIdFrom(), message.getAmount(),
                "", TransactionType.TRANSFER, Status.PENDING);
        Transaction transactionTo = createTransaction(message.getAccountIdTo(), message.getAmount(),
                "", TransactionType.TRANSFER, Status.PENDING);
        try{
            transactionRepository.save(transactionFrom);
            transactionRepository.save(transactionTo);
            TransferResultMessage messageResult = new TransferResultMessage(transactionFrom.getId(), transactionTo.getId(),
                    Status.SUCCESS, message.getAmount(), message.getAccountIdFrom(), message.getAccountIdTo());
            kafkaTemplate.send("transaction-start-callback-topic", messageResult);
        } catch (Exception e) {
            //логируем
            TransferResultMessage messageResult = new TransferResultMessage(transactionFrom.getId(), transactionTo.getId(),
                    Status.FAILED, message.getAmount(), message.getAccountIdFrom(), message.getAccountIdTo());
            kafkaTemplate.send("transaction-start-callback-topic", messageResult);
        }
    }

    @Transactional
    public void doTransferFinish(FinishMessage message) {
        //логируем
        Transaction transFrom = transactionRepository.getById(message.getTransactionIdFrom());
        Transaction transTo = transactionRepository.getById(message.getTransactionIdTo());
        //ставим status транзакций Success or Failed
        transFrom.setStatus(message.getStatus());
        transTo.setStatus(message.getStatus());
        try {
            transactionRepository.save(transFrom);
            transactionRepository.save(transFrom);
        } catch (Exception e) {
            //в случае проблем с обновлением статуса транзакции запустим шедулер по обновлению в success или failed
        }
    }

    public Transaction createTransaction(Long accountId, BigDecimal amount, String accountPeriod, TransactionType type,
                                         Status status) {
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setAccountPeriod(accountPeriod);
        transaction.setType(type);
        transaction.setStatus(status);
        return transaction;
    }

}
