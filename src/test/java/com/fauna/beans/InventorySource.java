package com.fauna.beans;

import com.fauna.e2e.beans.Product;
import com.fauna.event.EventSource;
import com.fauna.types.Page;

public class InventorySource {
    public Page<Product> firstPage;
    public EventSource eventSource;
}
