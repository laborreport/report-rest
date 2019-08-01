package ru.indraft.reportrest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserModel {

    @JsonProperty("contract_number")
    private String contractNumber;

    @JsonProperty("contract_date")
    private LocalDate contractDate;

    @JsonProperty("pe_series")
    private String peSeries;

    @JsonProperty("pe_number")
    private String peNumber;

    @JsonProperty("rate")
    private Double rate;

}
