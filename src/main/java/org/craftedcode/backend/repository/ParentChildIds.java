package org.craftedcode.backend.repository;

/** Projection for batch-loading child ids grouped by their parent entity id. */
public interface ParentChildIds {
  Long getParentId();

  Long getChildId();
}
