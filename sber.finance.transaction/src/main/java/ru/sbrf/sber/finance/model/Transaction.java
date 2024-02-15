package ru.sbrf.sber.finance.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sbrf.sber.finance.model.dtowrapper.Status;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long accountId;

    private BigDecimal amount;

    private String currency;

    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date date;

    private String accountPeriod;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private Status status;
}