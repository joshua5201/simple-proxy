package com.example.simpleproxy.model;

import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Upstream {
    private String protocol;
    private String host;
    private int port = 80;
}
