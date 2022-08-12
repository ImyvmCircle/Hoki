package com.imyvm.hoki.util;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class CommandUtil {
    private CommandUtil() {
    }

    public static Text getSuggestCommandText(MutableText text, String command) {
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(command));
        return text.styled((style) -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent));
    }

    public static Text getSuggestCommandText(String command) {
        return getSuggestCommandText(Text.literal(command), command);
    }

    public static Text getRunCommandText(MutableText text, String command) {
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(command));
        return text.styled((style) -> style.withClickEvent(clickEvent).withHoverEvent(hoverEvent));
    }

    public static Text getRunCommandText(String command) {
        return getRunCommandText(Text.literal(command), command);
    }
}
