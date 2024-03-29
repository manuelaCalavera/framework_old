package ch.usz.c3pro.questionnaire.logic;

import android.content.Intent;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.DateType;
import org.hl7.fhir.dstu3.model.IntegerType;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.StringType;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.BooleanAnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.answerformat.DateAnswerFormat;
import org.researchstack.backbone.answerformat.IntegerAnswerFormat;
import org.researchstack.backbone.answerformat.TextAnswerFormat;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.ui.ViewTaskActivity;

import java.util.Date;
import java.util.Map;

/**
 * C3PRO
 *
 * Created by manny Weber on 05/23/16.
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
 * This class provides the tools to create a FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse}
 * from a ResearchStack {@link org.researchstack.backbone.result.TaskResult}
 */
public class TaskResult2QuestionnaireResponse {

    /**
     * Returns a FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} based on the passed
     * ResearchStack {@link org.researchstack.backbone.result.TaskResult}
     *
     * @param taskResult The taskResult of the conducted survey produced by a {@link org.researchstack.backbone.ui.ViewTaskActivity}
     * @return A FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} that contains all the
     * question IDs with corresponding answers given by the user
     */
    public static QuestionnaireResponse taskResult2QuestionnaireResponse(TaskResult taskResult) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        Map<String, StepResult> stepResults = taskResult.getResults();
        questionnaireResponse.setId(taskResult.getIdentifier());

        for (Map.Entry<String, StepResult> entry : stepResults.entrySet()) {
            String key = entry.getKey();
            StepResult stepResult = entry.getValue();
            QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = stepResult2ResponseItem(stepResult);
            if (responseItem != null) {
                responseItem.setLinkId(key);
                questionnaireResponse.addItem(responseItem);
            }
        }

        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        return questionnaireResponse;
    }

    /**
     * Returns a FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemComponent} based on the passed
     * ResearchStack {@link org.researchstack.backbone.result.StepResult}
     *
     * @param stepResult An individual stepResult of a ResearchStack {@link org.researchstack.backbone.result.TaskResult}
     * @return A FHIR {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemComponent} that contains the
     * question ID with the corresponding answer given by the user
     */
    public static QuestionnaireResponse.QuestionnaireResponseItemComponent stepResult2ResponseItem(StepResult stepResult) {

        if ((stepResult != null) && (stepResult.getResult() != null)) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent responseItem = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
            responseItem.addAnswer(getFHIRAnswerForStepResult(stepResult));
            return responseItem;
        } else {
            return null;
        }
    }


    /*
    None(NotImplementedStepBody.class),
    Scale(NotImplementedStepBody.class),
    SingleChoice(SingleChoiceQuestionBody.class),
    MultipleChoice(MultiChoiceQuestionBody.class),
    Decimal(NotImplementedStepBody.class),
    Integer(IntegerQuestionBody.class),
    Boolean(SingleChoiceQuestionBody.class),
    Eligibility(NotImplementedStepBody.class),
    Text(TextQuestionBody.class),
    TimeOfDay(NotImplementedStepBody.class),
    DateAndTime(NotImplementedStepBody.class),
    Date(DateQuestionBody.class),
    TimeInterval(NotImplementedStepBody.class),
    Location(NotImplementedStepBody.class),
    Form(FormBody.class);
    * */

    /**
     * Returns a {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent}
     * based on the passed ResearchStack {@link org.researchstack.backbone.result.StepResult}
     *
     * @param stepResult A {@link org.researchstack.backbone.result.StepResult} containing an answer given by the user
     * @return A {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent} containing the answer given by the user
     */
    public static QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent getFHIRAnswerForStepResult(StepResult stepResult) {

        QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answerComponent = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();

        AnswerFormat format = stepResult.getAnswerFormat();

        if (format instanceof ChoiceAnswerFormat) {

            if (format instanceof BooleanAnswerFormat) {
                answerComponent.setValue(new BooleanType((Boolean) stepResult.getResult()));
            } else {
                // TODO ChoiceAnswers
                answerComponent.setValue(new StringType((String) stepResult.getResult()));
            }

        } else if (format instanceof IntegerAnswerFormat) {
            answerComponent.setValue(new IntegerType((int) stepResult.getResult()));
        } else if (format instanceof TextAnswerFormat) {
            answerComponent.setValue(new StringType((String) stepResult.getResult()));
        } else if (format instanceof DateAnswerFormat) {
            answerComponent.setValue(new DateType(new Date((long) stepResult.getResult())));
        }
        return answerComponent;
    }

    /**
     * Returns a {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} based on the
     * ResearchStack {@link org.researchstack.backbone.result.StepResult} within the passed
     * {@link Intent}. This is the Intent returned in the onActivityResult() method
     * of the parent activity of the {@link org.researchstack.backbone.ui.ViewTaskActivity}
     *
     * @param data The {@link Intent} returned by the {@link org.researchstack.backbone.ui.ViewTaskActivity}
     * @return A {@link org.hl7.fhir.dstu3.model.QuestionnaireResponse} containing the answers given by the user
     */
    public static QuestionnaireResponse resultIntent2QuestionnaireResponse(Intent data) {

        if (data != null) {
            TaskResult taskResult = (TaskResult) data.getExtras().get(ViewTaskActivity.EXTRA_TASK_RESULT);
            if (taskResult != null) {
                return taskResult2QuestionnaireResponse(taskResult);
            }
        }
        return null;
    }
}
