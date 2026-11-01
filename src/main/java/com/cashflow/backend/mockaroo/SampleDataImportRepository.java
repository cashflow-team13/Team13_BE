
package com.cashflow.backend.mockaroo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleDataImportRepository
        extends JpaRepository<SampleDataImport, Long> {

    boolean existsByAccountId(Long accountId);
}
