package org.dubini.gestion.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class JwtHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwtBuilder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwtParser");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwtParserBuilder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwsHeader");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultClaims");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultClaimsBuilder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultHeader");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwt");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJws");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwe");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultMutableJwsHeader");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultMutableJweHeader");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultProtectedHeader");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.ParameterMap");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJweHeaderMutator");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.DefaultJwsHeaderMutator");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardKeyOperations");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.KeyOperationConverter");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.AbstractJwk");
        registerClassIfExists(hints, "io.jsonwebtoken.security.Jwks");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardSecureDigestAlgorithms");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardKeyAlgorithms");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardHashAlgorithms");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardSignatureAlgorithms");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultKeyParser");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultKeyPairBuilder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultSecretKeyBuilder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultRsaKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultEcKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.EdwardsCurve");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultKeyOperationBuilder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultKeyRequest");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.KeysBridge");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultMacAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.HmacShaKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.StandardMacAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.HmacAesAeadAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultVerifySecureDigestRequest");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultSecureDigestAlgorithm");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.RsaSignatureAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.EcSignatureAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.EdSignatureAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultSignatureAlgorithm");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DefaultAeadAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.AesGcmKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.DirectKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.RsaKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.EcdhKeyAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.security.Pbes2HsAkwAlgorithm");
        
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.StandardCompressionAlgorithms");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.ConvertingParser");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.DelegateStringDecoder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.DelegateStringEncoder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.Base64UrlStreamEncoder");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.DeflateCompressionAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.GzipCompressionAlgorithm");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.RuntimeClasspathDeserializer");
        registerClassIfExists(hints, "io.jsonwebtoken.impl.io.RuntimeClasspathSerializer");
        
        registerClassIfExists(hints, "io.jsonwebtoken.jackson.io.JacksonDeserializer");
        registerClassIfExists(hints, "io.jsonwebtoken.jackson.io.JacksonSerializer");
        
        registerClassIfExists(hints, "io.jsonwebtoken.lang.Collections");
        registerClassIfExists(hints, "io.jsonwebtoken.lang.Strings");
        registerClassIfExists(hints, "io.jsonwebtoken.lang.Classes");
        registerClassIfExists(hints, "io.jsonwebtoken.lang.Arrays");
        registerClassIfExists(hints, "io.jsonwebtoken.lang.Assert");
        
        registerClassIfExists(hints, "javax.crypto.SecretKey");
        registerClassIfExists(hints, "javax.crypto.spec.SecretKeySpec");
        registerClassIfExists(hints, "java.security.Key");
        registerClassIfExists(hints, "java.security.PublicKey");
        registerClassIfExists(hints, "java.security.PrivateKey");
        
        hints.resources().registerPattern("META-INF/services/io.jsonwebtoken.*");
    }

    private void registerClassIfExists(RuntimeHints hints, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            hints.reflection().registerType(clazz, MemberCategory.values());
        } catch (ClassNotFoundException e) {
            // Class is optional or not present on classpath, ignore registering hints
        }
    }
}
