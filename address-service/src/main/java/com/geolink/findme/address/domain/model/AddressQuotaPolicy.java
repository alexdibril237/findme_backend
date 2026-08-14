package com.geolink.findme.address.domain.model;

import com.geolink.findme.address.domain.exception.AddressQuotaExceededException;

public final class AddressQuotaPolicy {

    public static final int MAX_ADDRESSES_PER_USER = 4;

    private AddressQuotaPolicy() {
    }

    public static void ensureCanCreate(long currentCount) {
        if (currentCount >= MAX_ADDRESSES_PER_USER) {
            throw new AddressQuotaExceededException(MAX_ADDRESSES_PER_USER);
        }
    }
}
