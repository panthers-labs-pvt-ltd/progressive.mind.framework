/**
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not
 * use this file except in compliance with the License. A copy of the License
 * is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.deequ.anomalydetection

import breeze.linalg.DenseVector
import org.scalatest.wordspec.AnyWordSpec

class RelativeRateOfChangeStrategyTest extends AnyWordSpec {

  "Relative Rate of Change Strategy" should {

    val strategy = RelativeRateOfChangeStrategy(Some(0.5), Some(2.0))
    val data = (for (i <- 0 to 50) yield {
      if (i < 20 || i > 30) {
        1.0
      } else {
        if (i % 2 == 0) i else 1
      }
    }).toVector

    "detect all anomalies if no interval specified" in {
      val anomalyResult = strategy.detect(data)
      val expected = for (i <- 20 to 31) yield {
        (i, Anomaly(Option(data(i)), 1.0))
      }
      assert(anomalyResult == expected)
    }

    "only detect anomalies in interval" in {
      val anomalyResult = strategy.detect(data, (25, 50))
      val expected = for (i <- 25 to 31) yield {
        (i, Anomaly(Option(data(i)), 1.0))
      }
      assert(anomalyResult == expected)
    }

    "ignore min rate if none is given" in {
      val strategy = RelativeRateOfChangeStrategy(None, Some(1.0))
      val anomalyResult = strategy.detect(data)

      // Anomalies with positive values only
      val expected = for (i <- 20 to 30 by 2) yield {
        (i, Anomaly(Option(data(i)), 1.0))
      }
      assert(anomalyResult == expected)
    }

    "ignore max rate if none is given" in {
      val strategy = RelativeRateOfChangeStrategy(Some(0.5), None)
      val anomalyResult = strategy.detect(data)

      // Anomalies with negative values only
      val expected = for (i <- 21 to 31 by 2) yield {
        (i, Anomaly(Option(data(i)), 1.0))
      }
      assert(anomalyResult == expected)
    }

    "detect no anomalies if rates are set to min/ max value" in {
      val strategy = RelativeRateOfChangeStrategy(Some(Double.MinValue), Some(Double.MaxValue))
      val anomalyResult = strategy.detect(data)

      val expected: List[(Int, Anomaly)] = List()
      assert(anomalyResult == expected)
    }

    "derive first order correctly" in {
      val data = DenseVector(1.0, 2.0, 4.0, 1.0, 2.0, 8.0)
      val result = strategy.diff(data, 1).data

      val expected = Array(2.0, 2.0, 0.25, 2.0, 4.0)
      assert(result === expected)
    }

    "derive second order correctly" in {
      val data = DenseVector(1.0, 2.0, 4.0, 1.0, 2.0, 8.0)
      val result = strategy.diff(data, 2).data

      val expected = Array(4.0, 0.5, 0.5, 8.0)
      assert(result === expected)
    }
    "derive third order correctly" in {
      val data = DenseVector(1.0, 5.0, -10.0, 3.0, 100.0, 0.01, 0.006)
      val result = strategy.diff(data, 3).data

      val expected = Array(3.0, 20.0, -0.001, 0.002)
      assert(result === expected)
    }

    "attribute indices correctly for higher orders without search interval" in {
      val data = Vector(0.0, 1.0, 3.0, 6.0, 18.0, 72.0)
      val strategy = RelativeRateOfChangeStrategy(None, Some(8.0), order = 2)
      val result = strategy.detect(data)

      val expected = Seq((2, Anomaly(Option(3.0), 1.0)), (5, Anomaly(Option(72.0), 1.0)))
      assert(result == expected)
    }

    "attribute indices correctly for higher orders with search interval" in {
      val data = Vector(0.0, 1.0, 3.0, 6.0, 18.0, 72.0)
      val strategy = RelativeRateOfChangeStrategy(None, Some(8.0), order = 2)
      val result = strategy.detect(data, (5, 6))

      val expected = Seq((5, Anomaly(Option(72.0), 1.0)))
      assert(result == expected)
    }


    "throw an error when rates aren't ordered" in {
      intercept[IllegalArgumentException] {
        RelativeRateOfChangeStrategy(Some(-2.0), Some(-3.0))
      }
    }

    "throw an error when no maximal rate given" in {
      intercept[IllegalArgumentException] {
        RelativeRateOfChangeStrategy(None, None)
      }
    }

    "work fine with empty input" in {
      val emptySeries = Vector[Double]()
      val anomalyResult = strategy.detect(emptySeries)

      assert(anomalyResult == Seq[(Int, Anomaly)]())
    }

    "produce error message with correct value and bounds" in {
      val result = strategy.detect(data)

      result.foreach { case (_, anom) =>
        val (value, lowerBound, upperBound) =
          AnomalyDetectionTestUtils.firstThreeDoublesFromString(anom.detail.get)

        assert(value -1 < lowerBound || value -1 > upperBound)
      }
    }
  }
}
