package org.dubini.gestion.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(TestHintsRegistrar.class)
public class TestHintsConfig {
}
