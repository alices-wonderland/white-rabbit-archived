package com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj;

import java.math.BigDecimal;
import java.util.UUID;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.lang.Nullable;

public record FinRecordItemInput(
    UUID accountId,
    @PositiveOrZero BigDecimal amount,
    @Nullable String buyingUnit,
    @Nullable BigDecimal buyingPrice,
    @Nullable String note) {}
