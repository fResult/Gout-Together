package dev.fResult.goutTogether.common.models;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record RSAKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey) {}
