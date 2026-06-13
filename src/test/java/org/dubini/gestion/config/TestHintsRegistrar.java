package org.dubini.gestion.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class TestHintsRegistrar implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName("org.springframework.test.context.web.WebAppConfiguration");
            hints.reflection().registerType(clazz, MemberCategory.values());
        } catch (ClassNotFoundException e) {
            // Ignore if not present
        }
    }
}
