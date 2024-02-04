package com.example.simpleproxy.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Upstream {
    private String protocol;
    private String host;
    private int port = 80;
}
