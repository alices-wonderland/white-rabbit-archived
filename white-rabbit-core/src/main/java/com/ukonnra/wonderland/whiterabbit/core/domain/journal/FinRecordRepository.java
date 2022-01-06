package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.FinRecord;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractRepository;

public interface FinRecordRepository
    extends AbstractRepository<FinRecord, FinRecord.PresentationModel> {}
