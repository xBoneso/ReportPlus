package me.xbones.reportplus.bungee;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class MessageCreateEventListener implements MessageCreateListener {

    private ReportPlus main;


    public MessageCreateEventListener(ReportPlus main){
        this.main=main;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent e) {
        String id = Long.toString(e.getChannel().getId());

        if((boolean)main.getValue("Enabled-Modules.Console", true)) {
            if (id.equals(main.getValue("Console-Channel-ID", "[INSERT ID HERE]"))){
                if (e.getMessage().getUserAuthor().get().isBot()) {
                    return;
                }
                main.getProxy().getPluginManager().dispatchCommand(main.getProxy().getConsole(), e.getMessage().getContent());


            }
        }
        if((boolean)main.getValue("Enabled-Modules.Chat-Sync", false)) {
            if(id.equals(main.getValue("Chat-Sync-Channel-ID", "[INSERT ID HERE]"))){
                if(e.getMessage().getUserAuthor().get().isBot()) return;
                TextComponent textComponent = new TextComponent();

                String format = (String)main.getValue("Discord-Chat-Sync-Format", "&b[Discord] &c%user% -> %message%");

                String msg = ChatColor.translateAlternateColorCodes('&',format.replace("%user%", e.getMessage().getAuthor().getDisplayName()));

                if(e.getMessage().getAttachments() != null && e.getMessage().getAttachments().size() > 0){
                    for(MessageAttachment attachment : e.getMessage().getAttachments()){
                        if(attachment.isImage()){
                            msg = msg.replace("%message%", attachment.getUrl().toString());
                            textComponent.setText(msg);
                            main.getProxy().broadcast(textComponent);
                        }
                    }
                    return;
                }
                msg = msg.replace("%message%", e.getMessage().getContent());


                textComponent.setText(ChatColor.translateAlternateColorCodes('&',msg));
                main.getProxy().broadcast(textComponent);
            }
        }
    }
}
