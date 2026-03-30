package com.imyvm.hoki.util;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class CommandUtil {
    private CommandUtil() {
    }

    public static Component getSuggestCommandText(MutableComponent text, String command) {
        ClickEvent clickEvent = new ClickEvent.SuggestCommand(command);
        HoverEvent hoverEvent = new HoverEvent.ShowText(Component.literal(command));
        return text.withStyle((style) -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent));
    }

    public static Component getSuggestCommandText(String command) {
        return getSuggestCommandText(Component.literal(command), command);
    }

    public static Component getRunCommandText(MutableComponent text, String command) {
        ClickEvent clickEvent = new ClickEvent.RunCommand(command);
        HoverEvent hoverEvent = new HoverEvent.ShowText(Component.literal(command));
        return text.withStyle((style) -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent));
    }

    public static Component getRunCommandText(String command) {
        return getRunCommandText(Component.literal(command), command);
    }
}
