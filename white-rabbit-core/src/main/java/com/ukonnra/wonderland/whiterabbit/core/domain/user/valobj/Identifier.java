package com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj;

import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import java.net.URL;
import java.util.Arrays;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class Identifier {
  @Column(nullable = false)
  private Type type;

  @Column(nullable = false)
  private String value;

  public Identifier(final Jwt jwt) {
    this.type = Type.of(jwt.getIssuer());
    this.value = jwt.getSubject();
  }

  @Getter
  public enum Type {
    AUTHING("https://wonderland-white-rabbit.authing.cn/oidc");

    private final String issuer;

    Type(final String issuer) {
      this.issuer = issuer;
    }

    public static Type of(final URL issuer) {
      return Arrays.stream(Type.values())
          .filter(type -> type.issuer.equals(issuer.toString()))
          .findFirst()
          .orElseThrow(CoreException.InvalidToken::new);
    }
  }
}
