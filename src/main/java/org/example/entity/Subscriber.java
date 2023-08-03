package org.example.entity;

import org.example.BlablaPluginsBuilder;

import java.util.HashSet;
import java.util.Set;

public class Subscriber {

    private String email;
    private Set<BlablaPluginsBuilder> plugins;

    public Subscriber(String email) {
        this.email = email;
        plugins = new HashSet<>();
    }

    public String getEmail() {
        return email;
    }

    public Set<BlablaPluginsBuilder> getPlugins() {

        return plugins;
    }
}
