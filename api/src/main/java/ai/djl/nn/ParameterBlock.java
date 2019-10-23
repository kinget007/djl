/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ai.djl.nn;

import ai.djl.Device;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;

public abstract class ParameterBlock extends AbstractBlock {

    @Override
    public Shape[] initialize(
            NDManager manager, DataType dataType, Device[] devices, Shape[] inputShapes) {
        if (!initialized) {
            beforeInitialize(inputShapes);
            for (Parameter parameter : getDirectParameters()) {
                parameter.initialize(manager, dataType, inputShapes);
            }
            initialized = true;
        }
        return getOutputShapes(manager, inputShapes);
    }

    @Override
    public BlockList getChildren() {
        return new BlockList();
    }
}