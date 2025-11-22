package com.pixelpro.dashboard.projection;

import java.math.BigDecimal;

/**
 * Proyección para consultas agregadas de productos más vendidos
 */
public interface TopProductProjection {
    Long getProductId();

    String getProductName();

    Long getQtySold();

    BigDecimal getRevenue();
}

