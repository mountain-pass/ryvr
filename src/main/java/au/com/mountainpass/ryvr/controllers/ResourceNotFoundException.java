package au.com.mountainpass.ryvr.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  private HttpServletRequest req;

  public ResourceNotFoundException(HttpServletRequest req) {
    super();
    this.req = req;
  }

}