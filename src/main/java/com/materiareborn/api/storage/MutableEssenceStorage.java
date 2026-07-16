package com.materiareborn.api.storage;

import com.materiareborn.api.essence.EssenceAmount;

public interface MutableEssenceStorage extends EssenceStorage {
    EssenceAmount receive(EssenceAmount amount, boolean simulate);

    EssenceAmount extract(EssenceAmount amount, boolean simulate);
}
