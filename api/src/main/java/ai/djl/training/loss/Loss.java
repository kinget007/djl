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
package ai.djl.training.loss;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.training.metrics.TrainingMetric;
import java.util.stream.IntStream;

/** Loss functions or Cost Functions to evaluate model predictions against true labels. */
public abstract class Loss extends TrainingMetric {

    private float totalLoss;
    private int totalInstances;
    private NDArray lastUpdate;

    /** Base class for metric with abstract update methods. */
    public Loss() {
        super("Loss");
    }

    /**
     * Calculate loss between label and prediction.
     *
     * @param label true label
     * @param prediction predicted label
     * @return loss value
     */
    public abstract NDArray getLoss(NDArray label, NDArray prediction);

    public static L1Loss l1Loss() {
        return new L1Loss();
    }

    public static L1Loss l1Loss(float weight, int batchAxis) {
        return new L1Loss(weight, batchAxis);
    }

    public static L2Loss l2Loss() {
        return new L2Loss();
    }

    public static L2Loss l2Loss(float weight, int batchAxis) {
        return new L2Loss(weight, batchAxis);
    }

    public static SigmoidBinaryCrossEntropyLoss sigmoidBinaryCrossEntropyLoss() {
        return new SigmoidBinaryCrossEntropyLoss();
    }

    public static SigmoidBinaryCrossEntropyLoss sigmoidBinaryCrossEntropyLoss(
            float weight, int batchAxis, boolean fromSigmoid) {
        return new SigmoidBinaryCrossEntropyLoss(weight, batchAxis, fromSigmoid);
    }

    public static SoftmaxCrossEntropyLoss softmaxCrossEntropyLoss() {
        return new SoftmaxCrossEntropyLoss();
    }

    public static SoftmaxCrossEntropyLoss softmaxCrossEntropyLoss(
            float weight, int batchAxis, int classAxis, boolean sparseLabel, boolean fromLogit) {
        return new SoftmaxCrossEntropyLoss(weight, batchAxis, classAxis, sparseLabel, fromLogit);
    }

    public static HingeLoss hingeLoss() {
        return new HingeLoss();
    }

    public static HingeLoss hingeLoss(int margin, float weight, int batchAxis) {
        return new HingeLoss(margin, weight, batchAxis);
    }

    @Override
    public Loss duplicate() {
        try {
            return (Loss) clone();
        } catch (CloneNotSupportedException e) {
            // ignore
            throw new AssertionError("Clone is not supported", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void update(NDList labels, NDList predictions) {
        // this is a synchronized operation, only call it at end of batch or epoch
        if (lastUpdate == null) {
            throw new IllegalStateException(
                    "You have not calculate loss yet, please "
                            + "call calculateLoss(labels, preds) before calling update().");
        }
        totalLoss += lastUpdate.sum().getFloat();
        totalInstances += lastUpdate.size();
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        totalLoss = 0.f;
        totalInstances = 0;
    }

    @Override
    public float getValue() {
        if (totalInstances == 0) {
            return Float.NaN;
        }
        return totalLoss / totalInstances;
    }

    /**
     * Helper function to get all axes except batch axis, loss functions requires reduction on all
     * axes except batch axis.
     *
     * @param loss loss {@code NDArray}
     * @param batchAxis axis that represents mini-batch
     * @return all axes except batch axis
     */
    protected int[] excludeBatchAxis(NDArray loss, int batchAxis) {
        return IntStream.range(0, loss.getShape().dimension())
                .filter(axis -> axis != batchAxis)
                .toArray();
    }

    /**
     * Calculate loss between label and prediction.
     *
     * <p>the default implementation is simply adding all losses together
     *
     * @param labels true labels
     * @param predictions predicted labels
     * @return loss value
     */
    public NDArray calculateLoss(NDList labels, NDList predictions) {
        // TODO: support composite loss for ssd (multi output)
        lastUpdate = getLoss(labels.head(), predictions.head());
        return lastUpdate;
    }
}