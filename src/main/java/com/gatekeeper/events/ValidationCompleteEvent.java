package com.gatekeeper.events;

import org.springframework.context.ApplicationEvent;

/**
 *
 * @author null
 */
public class ValidationCompleteEvent extends ApplicationEvent {

    public ValidationCompleteEvent(Object source) {
        super(source);
    }
}
