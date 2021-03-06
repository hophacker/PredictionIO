/** Copyright 2015 TappingStone, Inc.
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

package io.prediction.core

import io.prediction.controller.EngineParams
import io.prediction.controller.WorkflowParams

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.json4s.JValue

abstract class BaseEngine[EI, Q, P, A] extends Serializable {
  /** Implement this method so that training this engine would return a list of
    * models.
    *
    * @param sc An instance of SparkContext.
    * @param engineParams An instance of [[EngineParams]] for running a single training.
    * @param params An instance of [[WorkflowParams]] that controls the workflow.
    * @return A list of models.
    */
  def train(
    sc: SparkContext, 
    engineParams: EngineParams,
    engineInstanceId: String,
    params: WorkflowParams): Seq[Any]

  /** Implement this method so that [[io.prediction.controller.Evaluation]] can
    * use this method to generate inputs for [[io.prediction.controller.Metric]].
    *
    * @param sc An instance of SparkContext.
    * @param engineParams An instance of [[EngineParams]] for running a single evaluation.
    * @param params An instance of [[WorkflowParams]] that controls the workflow.
    * @return A list of evaluation information and RDD of query, predicted
    *         result, and actual result tuple tuple.
    */
  def eval(
    sc: SparkContext, 
    engineParams: EngineParams,
    params: WorkflowParams): Seq[(EI, RDD[(Q, P, A)])]

  /** Override this method to further optimize the process that runs multiple
    * evaluations (during tuning, for example). By default, this method calls
    * [[eval]] for each element in the engine parameters list.
    *
    * @param sc An instance of SparkContext.
    * @param engineParamsList A list of [[EngineParams]] for running batch evaluation.
    * @param params An instance of [[WorkflowParams]] that controls the workflow.
    * @return A list of engine parameters and evaluation result (from [[eval]]) tuples.
    */
  def batchEval(
    sc: SparkContext, 
    engineParamsList: Seq[EngineParams],
    params: WorkflowParams)
  : Seq[(EngineParams, Seq[(EI, RDD[(Q, P, A)])])] = {
    engineParamsList.map { engineParams => 
      (engineParams, eval(sc, engineParams, params))
    }
  }

  /** Implement this method to convert a JValue (read from an engine variant
    * JSON file) to an instance of [[EngineParams]].
    *
    * @param variantJson Content of the engine variant JSON as JValue.
    * @return An instance of [[EngineParams]] converted from JSON.
    */
  def jValueToEngineParams(variantJson: JValue): EngineParams =
    throw new NotImplementedError("JSON to EngineParams is not implemented.")
}
