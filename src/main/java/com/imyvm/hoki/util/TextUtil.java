package com.imyvm.hoki.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextUtil {
    private TextUtil() {
    }

    public static Component concat(Component... texts) {
        MutableComponent result = Component.empty();
        for (Component text : texts)
            result.append(text);
        return result;
    }
}
