package ru.sbrf.sber.finance.model.dtowrapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
@AllArgsConstructor
public class AccountTransferStartMessage {
    private Long accountIdFrom;
    private Long accountIdTo;
    private BigDecimal amount;
}