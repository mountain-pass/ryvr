package au.com.mountainpass.ryvr.model;

public interface Record {

  public Field getField(int i);

  public int size();

  public void setPosition(long l);

}
