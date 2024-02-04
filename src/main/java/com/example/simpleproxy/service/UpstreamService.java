package com.example.simpleproxy.service;

import com.example.simpleproxy.model.Upstream;

public interface UpstreamService {
    /* for some strategy, we might need request as the parameter to provide more info.
     * Such as session, 3-tuple or 5-tuple for affinity.
     * Now we only have round-robin strategy, so there are no params.
     */

    Upstream getNext();
}
