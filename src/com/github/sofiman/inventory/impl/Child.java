package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.api.models.Container;

interface Child {

    void assign(Container parent);
    void dismiss();
    Container getParent();
}
