package com.ukonnra.wonderland.whiterabbit.core.query;

import com.ukonnra.wonderland.whiterabbit.core.entity.AbstractEntity;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public abstract class Cursor {
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

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

  public static <T extends AbstractEntity> String of(final T entity) {
    return of(entity.getId());
  }
}
