package ru.sbrf.sber.finance.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sbrf.sber.finance.model.*;
import ru.sbrf.sber.finance.model.dtowrapper.AccountTransferStartMessage;
import ru.sbrf.sber.finance.model.dtowrapper.FinishMessage;
import ru.sbrf.sber.finance.model.dtowrapper.Status;
import ru.sbrf.sber.finance.model.dtowrapper.TransferResultMessage;
import ru.sbrf.sber.finance.repositiries.AccountRepository;

import java.math.BigDecimal;
@Service
public class AccountService {

    @Autowired
    private KafkaTemplate<String, AccountTransferStartMessage> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, FinishMessage> kafkaFinishTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public AccountTransferStartMessage doTransfer(Long account_id_from, Long account_id_to, BigDecimal amount) {
        // проверить блокировки обоих счетов
        Account accountFrom = accountRepository.findById(account_id_from).
                orElseThrow(() -> new RuntimeException("Account not found. Id = " + account_id_from));
        Account accountTo = accountRepository.findById(account_id_to).
                orElseThrow(() -> new RuntimeException("Account not found. Id = " + account_id_to));
        if (accountFrom.getBalance().compareTo(amount) < 0) {
            //вывести: Недостаточно средств
        }
        AccountTransferStartMessage message = new AccountTransferStartMessage(account_id_from, account_id_to, amount);
        // логируем отправку
        kafkaTemplate.send("transaction-start-topic", message);
        return message;
    }

    @Transactional
    public void doTransferFinish(TransferResultMessage message) {
        Account accountFrom = accountRepository.findById(message.getAccountIdFrom()).
                orElseThrow(() -> new RuntimeException("Account not found. Id = "+ message.getAccountIdFrom()));
        Account accountTo = accountRepository.findById(message.getAccountIdTo()).
                orElseThrow(() -> new RuntimeException("Account not found. Id = "+ message.getAccountIdTo()));
        if (accountFrom.getBalance().compareTo(message.getAmount()) < 0) {
            //логируем
            doRollBack(message);
        } else {
            //логируем
            accountFrom.setBalance(accountFrom.getBalance().add(message.getAmount()));
            accountTo.setBalance(accountTo.getBalance().add(message.getAmount()));
            //для сохранения транзакционности вызываем через бин
            doFinishTransaction(message);
        }

    }

    public void doFinishTransaction(TransferResultMessage message) {
        FinishMessage messageFinish = new FinishMessage(Status.SUCCESS, message.getTransactionIdFrom(), message.getAccountIdTo());
        //логируем
        kafkaFinishTemplate.send("transaction-finish-topic", messageFinish);
    }

    public void doRollBack(TransferResultMessage message) {
        FinishMessage messageFinish = new FinishMessage(Status.FAILED, message.getTransactionIdFrom(), message.getAccountIdTo());
        //логируем
        kafkaFinishTemplate.send("transaction-finish-topic", messageFinish);
    }
}
