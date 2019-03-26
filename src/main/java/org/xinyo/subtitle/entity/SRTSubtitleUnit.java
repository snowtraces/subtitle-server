package org.xinyo.subtitle.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class SRTSubtitleUnit implements Serializable {
    private Integer number;
    private int start;
    private int end;
    private List<String> text = new ArrayList<>();
}
