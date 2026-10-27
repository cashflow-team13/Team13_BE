
package com.cashflow.backend.mockaroo;

import com.cashflow.backend.transaction.dto.MockTransactionResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MockarooClient {

    private final RestClient restClient;
    private final String apiUrl;

    public MockarooClient(
            @Value("${mockaroo.api.url}") String apiUrl,
            @Value("${mockaroo.api.key}") String apiKey
    ) {
        this.apiUrl = apiUrl;

        this.restClient = RestClient.builder()
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    public List<MockTransactionResponse> getTransactions(int count) {

        if (count < 1 || count > 1000) {
            throw new IllegalArgumentException(
                    "거래내역 생성 개수는 1~1000 사이여야 합니다."
            );
        }

        List<MockTransactionResponse> transactions = restClient.get()
                .uri(apiUrl + "?schema=CashFlow_Transactions"
                        + "&count=" + count
                        + "&array=true")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<
                        List<MockTransactionResponse>>() {});

        if (transactions == null) {
            throw new IllegalStateException(
                    "Mockaroo API에서 데이터를 받지 못했습니다."
            );
        }

        return transactions;
    }
}
