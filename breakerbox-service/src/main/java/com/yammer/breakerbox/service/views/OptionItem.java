package com.yammer.breakerbox.service.views;

public class OptionItem {

    private final String displayText;
    private final String value;

    public OptionItem(String displayText, long value) {
        this.displayText = displayText;
        this.value = String.valueOf(value);
    }

    public String getText(){
        return displayText;
    }

    public String getValue(){
        return value;
    }
}
