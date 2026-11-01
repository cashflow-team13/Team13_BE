
package com.cashflow.backend.mockaroo;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "sample_data_import",
    uniqueConstraints = @UniqueConstraint(
        columnNames = "account_id"
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SampleDataImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    public SampleDataImport(Long accountId) {
        this.accountId = accountId;
    }
}
