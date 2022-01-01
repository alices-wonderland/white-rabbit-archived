package com.ukonnra.wonderland.whiterabbit.core.infrastructure.query;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.AbstractEntity;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.Nullable;

public abstract class Cursor {
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  private Cursor() {
    throw new IllegalStateException("Cursor is an utility class");
  }

  /**
   * Extract ID from Cursor, right now Cursor is <code>base64(id)</code>
   *
   * @param cursor The bast64 cursor string
   * @return Entity ID
   */
  public static UUID extractId(final String cursor) {
    return UUID.fromString(new String(DECODER.decode(cursor), StandardCharsets.UTF_8));
  }

  public static String of(final UUID id) {
    return ENCODER.encodeToString(id.toString().getBytes(StandardCharsets.UTF_8));
  }

  public static <E extends AbstractEntity<?>> Optional<String> of(final @Nullable E entity) {
    return Optional.ofNullable(entity).flatMap(e -> Optional.ofNullable(e.getId())).map(Cursor::of);
  }
}
