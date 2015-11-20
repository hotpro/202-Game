package com.alfray.asqare.component_i;

/**
 * Created by ivanybma on 11/17/15.
 */
import java.util.HashMap;
public class BpPanelMenu {

    private HashMap<String, BpPanelMenuItem> menuItems = new HashMap<String, BpPanelMenuItem>();

    public void addMenuItem(BpPanelMenuItem item, String key){
        menuItems.put(key, item);
    }

    public void selectMenuItem(String key){

        BpPanelMenuItem item = menuItems.get(key);
        if(item!=null)
            item.invoke();
        else
            System.out.println("Menu Item Not Yet implemented: "+key);

    }
}
