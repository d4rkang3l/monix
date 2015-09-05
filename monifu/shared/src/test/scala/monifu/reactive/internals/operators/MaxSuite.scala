/*
 * Copyright (c) 2014-2015 by its authors. Some rights reserved.
 * See the project homepage at: http://www.monifu.org
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

package monifu.reactive.internals.operators

import monifu.reactive.Ack.Continue
import monifu.reactive.{Observer, Observable}
import scala.concurrent.duration.Duration.Zero
import scala.util.Success

object MaxSuite extends BaseOperatorSuite {
  def createObservable(sourceCount: Int) = Some {
    val o = Observable.range(0, sourceCount+1).max
    Sample(o, count(sourceCount), sum(sourceCount), Zero, Zero)
  }

  def observableInError(sourceCount: Int, ex: Throwable) = Some {
    val o = Observable.create[Long] { subscriber =>
      implicit val s = subscriber.scheduler
      val source = createObservableEndingInError(Observable.range(0, sourceCount+1), ex).max

      subscriber.onNext(sum(sourceCount)).onComplete {
        case Success(Continue) =>
          source.subscribe(subscriber)
        case _ =>
          ()
      }
    }

    Sample(o, count(sourceCount), sum(sourceCount), Zero, Zero)
  }

  def count(sourceCount: Int) = 1
  def sum(sourceCount: Int) = sourceCount
  def brokenUserCodeObservable(sourceCount: Int, ex: Throwable) = None

  test("empty observable should be empty") { implicit s =>
    val source: Observable[Long] = Observable.empty
    var received = 0
    var wasCompleted = false

    source.max.onSubscribe(new Observer[Long] {
      def onNext(elem: Long) = { received += 1; Continue }
      def onError(ex: Throwable) = ()
      def onComplete() = { wasCompleted = true }
    })

    assertEquals(received, 0)
    assert(wasCompleted)
  }
}
