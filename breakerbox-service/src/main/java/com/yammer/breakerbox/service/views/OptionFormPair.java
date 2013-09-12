package com.yammer.breakerbox.service.views;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Wrapper around a pair to flavor get methods for reflective calls in views
 */
public class OptionFormPair extends MutablePair<String, String> {

    public OptionFormPair(String displayText, long value) {
        super(displayText, String.valueOf(value));
    }

    public String getText(){
        return getLeft();
    }

    public String getValue(){
        return getRight();
    }
}
