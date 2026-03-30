package com.imyvm.hoki.i18n;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// CHECKSTYLE SUPPRESS: HideUtilityClassConstructor
public class HokiTranslator {
    private static final String FORMAT_PLACEHOLDER = "(?<!\\{)\\{(\\d+)}";
    private static final Pattern FORMAT_PATTERN = Pattern.compile(FORMAT_PLACEHOLDER);

    public static Component translate(HokiLanguage language, String key, Object... args) {
        String format = language.get(key);
        return textFormat(format, args);
    }

    private static Component textFormat(String format, Object[] args) {
        MutableComponent text = Component.empty();
        Matcher matcher = FORMAT_PATTERN.matcher(format);
        int previous = 0;
        while (matcher.find()) {
            text.append(format.substring(previous, matcher.start()));
            previous = matcher.end();

            int index = Integer.parseInt(matcher.group(1));
            text.append(objectToText(args[index]));
        }

        text.append(format.substring(previous));
        return text;
    }

    private static Component objectToText(Object obj) {
        if (obj instanceof Component text)
            return text;
        return Component.literal(String.valueOf(obj));
    }
}
