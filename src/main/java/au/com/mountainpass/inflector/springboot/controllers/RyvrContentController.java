package au.com.mountainpass.inflector.springboot.controllers;

import javax.ws.rs.core.MediaType;

import org.springframework.core.Ordered;

public interface RyvrContentController extends RyvrController, Ordered {

    public boolean isCompatible(MediaType type);

}
