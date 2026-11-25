package com.volt.usuariosauth.security;

import java.time.Instant;

public record IssuedToken(String value, Instant expiresAt) {
}
