
package com.yammer.breakerbox.turbine.model;

import java.util.List;

public class App {

    private Container container;
    private List<Task> tasks = null;

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

}
