package me.xbones.reportplus.core.chatcomponentapi;


import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatComponentMessage {

    private String text;
    private TextComponent component;

    public ChatComponentMessage(String text){
        this.text=text;
        component = new TextComponent(text);
    }

    public String getText() {
        return text;
    }

    public TextComponent getComponent() {
        return component;
    }

    public void addHover(String hover){
        component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(text).create()));
    }

    public void addClick(ClickEvent.Action action,String value)
    {
        component.setClickEvent(new ClickEvent(action,value));
    }


}
