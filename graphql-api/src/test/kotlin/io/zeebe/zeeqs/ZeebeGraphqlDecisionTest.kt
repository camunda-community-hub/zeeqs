package io.zeebe.zeeqs

import io.zeebe.zeeqs.data.entity.Decision
import io.zeebe.zeeqs.data.entity.DecisionRequirements
import io.zeebe.zeeqs.data.repository.DecisionRepository
import io.zeebe.zeeqs.data.repository.DecisionRequirementsRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.graphql.test.tester.GraphQlTester
import java.time.Instant


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConfiguration
class ZeebeGraphqlDecisionTest(
    @Autowired private val graphQlTester: GraphQlTester,
    @Autowired private val decisionRepository: DecisionRepository,
    @Autowired private val decisionRequirementsRepository: DecisionRequirementsRepository
) {

    private val decisionRequirementsKey = 10L
    private val decisionKey = 20L

    @BeforeEach
    fun `deploy decision`() {
        decisionRequirementsRepository.save(
            DecisionRequirements(
                key = decisionRequirementsKey,
                decisionRequirementsId = "Ratings",
                decisionRequirementsName = "Rating example",
                version = 1,
                namespace = "namespace",
                dmnXML = "<xml>",
                deployTime = Instant.now().toEpochMilli(),
                resourceName = "rating.dmn",
                checksum = "checksum"
            )
        )

        decisionRepository.save(
            Decision(
                key = decisionKey,
                decisionId = "decision_a",
                decisionName = "Decision A",
                version = 1,
                decisionRequirementsKey = decisionRequirementsKey,
                decisionRequirementsId = "Rating example"
            )
        )
    }

    @Test
    fun `should query decision`() {
        // when/then
        graphQlTester.document(
            """
                    {
                      decisions {
                        nodes {
                          key
                          decisionId
                          decisionRequirements {
                            key
                          }
                        }
                      }
                    }
                    """
        )
            .execute()
            .path("decisions.nodes")
            .matchesJson(
                """
                    [
                        {
                          "key": "$decisionKey",
                          "decisionId": "decision_a",
                          "decisionRequirements": {
                            "key": "$decisionRequirementsKey"
                          }
                        }
                    ]              
                    """
            )
    }

    @Test
    fun `should query decision requirements`() {
        // when/then
        graphQlTester.document(
            """
                    {
                      decisionRequirements {
                        nodes {
                          key
                          decisionRequirementsId
                          decisions {
                            key
                          }
                        }
                      }
                    }
                    """
        )
            .execute()
            .path("decisionRequirements.nodes")
            .matchesJson(
                """
                    [
                        {
                          "key": "$decisionRequirementsKey",
                          "decisionRequirementsId": "Ratings",
                          "decisions": [
                            { "key": "$decisionKey" }
                          ]
                        }
                    ]              
                    """
            )
    }

    @SpringBootApplication
    class TestConfig

}