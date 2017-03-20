package au.com.mountainpass.ryvr.model;

import org.springframework.stereotype.Component;

@Component
public class RyvrsCollection extends MutableHalRepresentation {

    private static final String TITLE = "Ryvrs";

    public int getCount() {
        return super.getEmbedded().getItemsBy("item").size();
    }

    public String getTitle() {
        return TITLE;
    }

}
