/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.spring.data.gremlin.conversion;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GremlinEdgeScriptLiteralFactory implements GremlinScriptFactory {

    @Override
    public GremlinEdgeScriptLiteral createGremlinScript() {
        return new GremlinEdgeScriptLiteral();
    }
}