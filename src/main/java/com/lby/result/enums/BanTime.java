package com.lby.result.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BanTime {
    DAY(60L * 60L * 24L,"以天为单位",1),
    MONTH(60L * 60L * 24L * 30L,"以月为单位",2),
    YEARS(60L * 60L * 24L * 365L,"以年为单位",3);

    private Long times;
    private String description;

    @JsonValue
    private Integer type;


}
