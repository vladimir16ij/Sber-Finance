package ru.sbrf.sber.finance.model.dtowrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResultMessage {
    private UUID transactionIdFrom;
    private UUID transactionIdTo;
    private Status status;
    private BigDecimal amount;
    private Long accountIdFrom;
    private Long accountIdTo;
}
