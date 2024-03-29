package ch.usz.c3pro.questionnaire.logic;

import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.OrderedTask;

import java.io.Serializable;
import java.util.List;

/**
 * C3PRO
 *
 * Created by manny Weber on 05/18/16.
 * Copyright © 2016 University Hospital Zurich. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This class extends the ResearchStack {@link org.researchstack.backbone.task.OrderedTask} and can
 * display {@link org.researchstack.backbone.step.Step}s as well as
 * {@link ConditionalStep}s. ConditionalSteps can have {@link ResultRequirement}s and are
 * only shown to the user when all of them are met.
 * The logic is derived from the FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemEnableWhenComponent} element.
 */
public class ConditionalOrderedTask extends OrderedTask implements Serializable {

    /**
     * The parent class {@link org.researchstack.backbone.task.OrderedTask} has no default constructor, so we have to provide one.
     * This constructor should not be used.
     */
    private ConditionalOrderedTask() {
        super("", new ConditionalQuestionStep("", "", new TextAnswerFormat()));
    }

    /**
     * Consturctor.
     * Returns an initialized ConditionalOrderedTask using the specified identifier and array of steps.
     *
     * @param identifier The unique identifier for the task. Should be identical to the the LinkId
     *                   of the corresponding FHIR {@link org.hl7.fhir.dstu3.model.Questionnaire}
     * @param steps     An array of {@link org.researchstack.backbone.step.Step}s and
     *                  {@link ConditionalStep}s in the order in which they should be presented.
     */
    public ConditionalOrderedTask(String identifier, List<Step> steps) {
        super(identifier, steps);
    }

    /**
     * Returns the next step that has all its requirements met by the provided {@link org.researchstack.backbone.result.TaskResult},
     * or null
     *
     * @param step   The reference step. Pass null to specify the first step.
     * @param result A snapshot of the current set of results.
     * @return the next step in <code>steps</code> after the passed step that has all its
     * requirements met by the provided {@link org.researchstack.backbone.result.TaskResult}, or null if at the end
     */
    @Override
    public Step getStepAfterStep(Step step, TaskResult result) {
        Step checkStep = null;
        if (step == null) {
            checkStep = steps.get(0);
        } else {
            int nextIndex = steps.indexOf(step) + 1;

            if (nextIndex < steps.size()) {
                checkStep = steps.get(nextIndex);
            }
        }

        if (checkStep instanceof ConditionalStep) {
            if (((ConditionalStep) checkStep).requirementsAreSatisfiedBy(result)) {
                return checkStep;
            } else {
                return getStepAfterStep(checkStep, result);
            }
        } else {
            return checkStep;
        }
    }

    /**
     * Returns the next step before the passed step that has all its requirements met by the
     * provided {@link org.researchstack.backbone.result.TaskResult}, or null
     *
     * @param step   The reference step.
     * @param result A snapshot of the current set of results.
     * @return the next step in <code>steps</code> before the passed step that has all its
     * requirements met by the provided {@link org.researchstack.backbone.result.TaskResult}, or null if at the start
     */
    @Override
    public Step getStepBeforeStep(Step step, TaskResult result) {
        Step checkStep = null;

        int nextIndex = steps.indexOf(step) - 1;

        if (nextIndex >= 0) {
            checkStep = steps.get(nextIndex);
        }

        if (checkStep instanceof ConditionalStep) {
            if (((ConditionalStep) checkStep).requirementsAreSatisfiedBy(result)) {
                return checkStep;
            } else {
                return getStepBeforeStep(checkStep, result);
            }
        } else {
            return checkStep;
        }
    }
}