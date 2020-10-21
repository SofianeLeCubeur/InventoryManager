package com.github.sofiman.inventory.impl;

import com.github.sofiman.inventory.utils.Callback;

public interface APIResponse<T> {

    void response(T callback);

    void error(RequestError error);
}
