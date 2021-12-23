package com.ukonnra.wonderland.whiterabbit.core.entity;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity(name = "books")
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public final class Book extends AbstractEntity {
  private String name;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @ToString.Exclude
  private User author;
}
