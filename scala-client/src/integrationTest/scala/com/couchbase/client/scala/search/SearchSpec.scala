/*
 * Copyright (c) 2019 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.scala.search

import com.couchbase.client.scala.env.ClusterEnvironment
import com.couchbase.client.scala.json.JsonObject
import com.couchbase.client.scala.kv.MutationState
import com.couchbase.client.scala.manager.search.{SearchIndex, SearchIndexNotFoundException}
import com.couchbase.client.scala.search.queries.{MatchAllQuery, SearchQuery}
import com.couchbase.client.scala.util.ScalaIntegrationTest
import com.couchbase.client.scala.{Cluster, Collection}
import com.couchbase.client.test.{Capabilities, ClusterAwareIntegrationTest, IgnoreWhen}
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api._

import scala.util.{Failure, Success}

@IgnoreWhen(missesCapabilities = Array(Capabilities.SEARCH))
@TestInstance(Lifecycle.PER_CLASS)
class SearchSpec extends ScalaIntegrationTest {

  private var cluster: Cluster  = _
  private var coll: Collection  = _
  private var ms: MutationState = _

  @BeforeAll
  def beforeAll(): Unit = {
    cluster = connectToCluster()
    val bucket = cluster.bucket(config.bucketname)
    coll = bucket.defaultCollection

    val result =
      coll.insert("test", JsonObject("name" -> "John Smith", "address" -> "123 Fake Street")).get

    ms = MutationState(Seq(result.mutationToken.get))

    val index = SearchIndex(indexName, config.bucketname)
    cluster.searchIndexes.upsertIndex(index).get
  }

  private def indexName = "idx-" + config.bucketname

  @AfterAll
  def afterAll(): Unit = {
    cluster.searchIndexes.dropIndex(indexName)
    cluster.disconnect()
  }

  @Timeout(120)
  @Test
  def simple() {
    def recurse(): Unit = {
      cluster.searchQuery(
        indexName,
        SearchQuery.matchPhrase("John Smith"),
        SearchOptions().scanConsistency(SearchScanConsistency.ConsistentWith(ms))
      ) match {
        case Success(result) =>
          result.metaData.errors.foreach(err => println(s"Err: ${err}"))
          assert(1 == result.rows.size)
          assert(result.rows.head.id == "test")
        case Failure(ex) =>
          println(ex.getMessage)
          if (ex.getMessage.contains("no planPIndexes for indexName") || ex.getMessage.contains(
                "pindex_consistency mismatched partition"
              )) {
            Thread.sleep(250)
            recurse()
          } else {
            assert(false)
          }
      }
    }

    recurse()
  }
}
