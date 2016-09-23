/*Copyright 2015 Robin East
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
package benchmark.classification

import org.apache.spark.{SparkContext,SparkConf}
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MLUtils

object Benchmark extends App {
  val conf = new SparkConf().setAppName("Classification Benchmark")
  val sc = new SparkContext(conf)
  
  // Load training data in LIBSVM format.
  var datafile_path = "/data/rcv1_test.binary"
  if (args.length > 0) datafile_path = args(0)
  val data_orig = MLUtils.loadLibSVMFile(sc, datafile_path)
  val data = 
    if (args.contains("nocache"))  data_orig.map(point => correct(point));
    else                           data_orig.map(point => correct(point)).cache();
  
  // Run training algorithm to build the model
  val numIterations = 100
  val model = time { SVMWithSGD.train(data, numIterations) }


  def correct(x: LabeledPoint): LabeledPoint = {
    if (x.label == -1)
      LabeledPoint(0, x.features)
    else
      x
  }

  def time[R](block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    val elapsed = t1 - t0
    println(s"Elapsed time: ${math.round(elapsed/1000000000.0)}s (${elapsed}ns)")
    result
  }
}
