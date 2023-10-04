package org.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserData {
    private Long chatId;
    private String destinationFrom;
    private String destinationTo;
    private String isCash;
    private String price;
    private String time;
}
