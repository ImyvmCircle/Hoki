package com.imyvm.hoki.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil {
    private TextUtil() {
    }

    public static Text concat(Text ...texts) {
        MutableText result = Text.empty();
        for (Text text : texts)
            result.append(text);
        return result;
    }
}
